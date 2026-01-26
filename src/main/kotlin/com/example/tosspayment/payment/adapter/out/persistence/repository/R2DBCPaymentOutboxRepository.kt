package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.common.objectMapper
import com.example.tosspayment.payment.adapter.out.stream.util.PartitionKeyUtil
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.domain.Outboxes
import com.example.tosspayment.payment.domain.PaymentEventMessage
import com.example.tosspayment.payment.domain.PaymentEventMessageType
import com.example.tosspayment.payment.domain.PaymentStatus
import com.example.tosspayment.payment.domain.Status
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime

@Repository
class R2DBCPaymentOutboxRepository(
    private val database: R2dbcDatabase,
    private val partitionKeyUtil: PartitionKeyUtil
) : PaymentOutboxRepository {

    override suspend fun insertOutbox(command: PaymentStatusUpdateCommand): PaymentEventMessage {
        require(command.status == PaymentStatus.SUCCESS)

        val paymentEventMessage = createPaymentEventMessage(command)

        suspendTransaction(db = database) {
            Outboxes.insert {
                it[idempotencyKey] = paymentEventMessage.payload["orderId"] as String
                it[partitionKey] = (paymentEventMessage.metadata["partitionKey"] as? Int) ?: 0
                it[type] = paymentEventMessage.type.name
                it[payload] = objectMapper.writeValueAsString(paymentEventMessage.payload)
                it[metadata] = objectMapper.writeValueAsString(paymentEventMessage.metadata)
            }
        }

        return paymentEventMessage
    }

    override fun markMessageAsSent(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean> {
        return mono {
            suspendTransaction(db = database) {
                Outboxes.update({ (Outboxes.idempotencyKey eq idempotencyKey) and (Outboxes.type eq type.name) }) {
                    it[status] = Status.SUCCESS
                    it[updatedAt] = LocalDateTime.now().toKotlinLocalDateTime()
                }
            }
            true
        }
    }

    override fun markMessageAsFailure(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean> {
        return mono {
            suspendTransaction(db = database) {
                Outboxes.update({ (Outboxes.idempotencyKey eq idempotencyKey) and (Outboxes.type eq type.name) }) {
                    it[status] = Status.FAILURE
                    it[updatedAt] = LocalDateTime.now().toKotlinLocalDateTime()
                }
            }
            true
        }
    }

    override suspend fun getPendingPaymentOutboxes(): List<PaymentEventMessage> {
        val oneMinuteAgo = LocalDateTime.now().minusMinutes(1).toKotlinLocalDateTime()

        return suspendTransaction(db = database) {
            Outboxes.selectAll()
                .where {
                    ((Outboxes.status eq Status.INIT) or (Outboxes.status eq Status.FAILURE)) and
                    (Outboxes.createdAt lessEq oneMinuteAgo) and
                    (Outboxes.type eq PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS.name)
                }
                .toList()
                .map { row ->
                    PaymentEventMessage(
                        type = PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS,
                        payload = objectMapper.readValue<Map<String, Any>>(row[Outboxes.payload] ?: "{}"),
                        metadata = objectMapper.readValue<Map<String, Any>>(row[Outboxes.metadata] ?: "{}")
                    )
                }
        }
    }

    private fun createPaymentEventMessage(command: PaymentStatusUpdateCommand): PaymentEventMessage {
        return PaymentEventMessage(
            type = PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS,
            payload = mapOf(
                "orderId" to command.orderId
            ),
            metadata = mapOf(
                "partitionKey" to partitionKeyUtil.createPartitionKey(command.orderId.hashCode())
            )
        )
    }
}
