package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.payment.domain.PaymentEvents
import com.example.tosspayment.payment.domain.PaymentOrderHistories
import com.example.tosspayment.payment.domain.PaymentOrders
import com.example.tosspayment.payment.domain.PaymentStatus
import com.example.tosspayment.payment.adapter.out.persistence.exception.PaymentAlreadyProcessedException
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.domain.PaymentEventMessagePublisher
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import org.springframework.stereotype.Repository
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

@Repository
class R2DBCPaymentStatusUpdateRepository(
    private val database: R2dbcDatabase,
    private val paymentOutboxRepository: PaymentOutboxRepository,
    private val paymentEventMessagePublisher: PaymentEventMessagePublisher
) : PaymentStatusUpdateRepository {

    /**
     * 결제 상태를 EXECUTING으로 업데이트
     *
     * 1. 이전 결제 상태 확인 (이미 처리된 결제인지 검증)
     * 2. 결제 히스토리 저장
     * 3. 결제 주문 상태 업데이트
     * 4. 결제 키 업데이트
     */
    override suspend fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Boolean {
        return suspendTransaction(db = database) {
            // 1. 이전 상태 확인 및 검증
            val previousStatus = checkPreviousPaymentOrderStatus(orderId)

            // 2. 히스토리 저장
            insertPaymentHistory(previousStatus, PaymentStatus.EXECUTING, "PAYMENT_CONFIRMATION_START")

            // 3. 결제 주문 상태 업데이트
            updatePaymentOrderStatus(orderId, PaymentStatus.EXECUTING)

            // 4. 결제 키 업데이트
            updatePaymentKey(orderId, paymentKey)

            true
        }
    }

    /**
     * 이전 결제 상태를 확인하고 유효성 검증
     * - NOT_STARTED, UNKNOWN, EXECUTING: 진행 가능
     * - SUCCESS: 이미 성공 처리됨 → 예외
     * - FAILURE: 이미 실패 처리됨 → 예외
     */
    private suspend fun checkPreviousPaymentOrderStatus(orderId: String): List<Pair<Long, PaymentStatus>> {
        val paymentOrders = selectPaymentOrderStatus(orderId)

        paymentOrders.forEach { (_, status) ->
            when (status) {
                PaymentStatus.SUCCESS -> throw PaymentAlreadyProcessedException(
                    message = "이미 처리 성공한 결제입니다.",
                    status = PaymentStatus.SUCCESS
                )

                PaymentStatus.FAILURE -> throw PaymentAlreadyProcessedException(
                    message = "이미 처리 실패한 결제입니다.",
                    status = PaymentStatus.FAILURE
                )
                // NOT_STARTED, UNKNOWN, EXECUTING은 계속 진행
                else -> { /* 진행 가능 */
                }
            }
        }

        return paymentOrders
    }

    /**
     * 결제 주문 상태 조회
     */
    private suspend fun selectPaymentOrderStatus(orderId: String): List<Pair<Long, PaymentStatus>> {
        return PaymentOrders
            .select(PaymentOrders.id, PaymentOrders.paymentOrderStatus)
            .where(PaymentOrders.orderId eq orderId)
            .toList()
            .map { row ->
                Pair(row[PaymentOrders.id], row[PaymentOrders.paymentOrderStatus])
            }
    }

    /**
     * 결제 히스토리 일괄 저장
     */
    private suspend fun insertPaymentHistory(
        paymentOrderIdToStatus: List<Pair<Long, PaymentStatus>>,
        newStatus: PaymentStatus,
        reason: String
    ) {
        if (paymentOrderIdToStatus.isEmpty()) return

        PaymentOrderHistories.batchInsert(paymentOrderIdToStatus) { (paymentOrderId, previousStatus) ->
            this[PaymentOrderHistories.paymentOrderId] = paymentOrderId
            this[PaymentOrderHistories.previousStatus] = previousStatus
            this[PaymentOrderHistories.newStatus] = newStatus
            this[PaymentOrderHistories.reason] = reason
            this[PaymentOrderHistories.createdAt] = LocalDateTime.now().toKotlinLocalDateTime()
        }
    }

    /**
     * 결제 주문 상태 업데이트
     */
    private suspend fun updatePaymentOrderStatus(orderId: String, status: PaymentStatus): Int {
        return PaymentOrders.update({ PaymentOrders.orderId eq orderId }) {
            it[paymentOrderStatus] = status
            it[updatedAt] = LocalDateTime.now().toKotlinLocalDateTime()
        }
    }

    /**
     * 결제 이벤트에 paymentKey 업데이트
     */
    private suspend fun updatePaymentKey(orderId: String, paymentKey: String): Int {
        return PaymentEvents.update({ PaymentEvents.orderId eq orderId }) {
            it[PaymentEvents.paymentKey] = paymentKey
        }
    }

    /**
     * 결제 상태 업데이트 (SUCCESS, FAILURE, UNKNOWN)
     */
    override suspend fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Boolean {
        when (command.status) {
            PaymentStatus.SUCCESS -> updatePaymentStatusToSuccess(command)
            PaymentStatus.FAILURE -> updatePaymentStatusToFailure(command)
            PaymentStatus.UNKNOWN -> updatePaymentStatusToUnknown(command)
            else -> error("결제 상태 (status: ${command.status}) 는 올바르지 않은 결제 상태입니다.")
        }
        return true
    }

    /**
     * 결제 성공 상태로 업데이트
     */
    private suspend fun updatePaymentStatusToSuccess(command: PaymentStatusUpdateCommand) {
        suspendTransaction(db = database) {
            // 1. 이전 상태 조회
            val previousStatus = selectPaymentOrderStatus(command.orderId)

            // 2. 히스토리 저장
            insertPaymentHistory(previousStatus, PaymentStatus.SUCCESS, "PAYMENT_CONFIRMATION_DONE")

            // 3. 결제 주문 상태 업데이트
            updatePaymentOrderStatus(command.orderId, PaymentStatus.SUCCESS)

            // 4. PaymentEvent에 성공 정보 업데이트
            val extraDetails = command.extraDetails!!
            PaymentEvents.update({ PaymentEvents.orderId eq command.orderId }) {
                it[isPaymentDone] = true
                it[method] = extraDetails.method
                it[pspRawData] = extraDetails.pspRawData
                it[approvedAt] = extraDetails.approvedAt
                it[updatedAt] = LocalDateTime.now().toKotlinLocalDateTime()
            }

            // 5. transaction outbox insert
            val paymentEventMessage = paymentOutboxRepository.insertOutbox(command)
            paymentEventMessagePublisher.publishEvent(paymentEventMessage)
        }
    }

    /**
     * 결제 실패 상태로 업데이트
     */
    private suspend fun updatePaymentStatusToFailure(command: PaymentStatusUpdateCommand) {
        suspendTransaction(db = database) {
            // 1. 이전 상태 조회
            val previousStatus = selectPaymentOrderStatus(command.orderId)

            // 2. 히스토리 저장
            insertPaymentHistory(previousStatus, PaymentStatus.FAILURE, "PAYMENT_CONFIRMATION_FAILED")

            // 3. 결제 주문 상태 업데이트
            updatePaymentOrderStatus(command.orderId, PaymentStatus.FAILURE)

            // 4. 실패 횟수 증가
            incrementFailedCount(command.orderId)
        }
    }

    /**
     * 결제 알 수 없는 상태로 업데이트
     */
    private suspend fun updatePaymentStatusToUnknown(command: PaymentStatusUpdateCommand) {
        suspendTransaction(db = database) {
            // 1. 이전 상태 조회
            val previousStatus = selectPaymentOrderStatus(command.orderId)

            // 2. 히스토리 저장
            insertPaymentHistory(previousStatus, PaymentStatus.UNKNOWN, "PAYMENT_CONFIRMATION_UNKNOWN")

            // 3. 결제 주문 상태 업데이트
            updatePaymentOrderStatus(command.orderId, PaymentStatus.UNKNOWN)

            // 4. 실패 횟수 증가 (재시도 대상이므로)
            incrementFailedCount(command.orderId)
        }
    }

    /**
     * 실패 횟수 증가
     */
    private suspend fun incrementFailedCount(orderId: String): Int {
        return PaymentOrders.update({ PaymentOrders.orderId eq orderId }) {
            it[failedCount] = failedCount + 1
        }
    }
}
