package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.payment.domain.PaymentEvents
import com.example.tosspayment.payment.domain.PaymentOrders
import com.example.tosspayment.payment.domain.PaymentStatus
import com.example.tosspayment.payment.domain.PaymentType
import com.example.tosspayment.payment.domain.PaymentEvent
import com.example.tosspayment.payment.domain.PendingPaymentEvent
import com.example.tosspayment.payment.domain.PendingPaymentOrder
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.springframework.stereotype.Repository
import java.time.LocalDateTime as JavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime

@Repository
class R2DBCPaymentRepository(
    private val database: R2dbcDatabase
) : PaymentRepository {

    override suspend fun save(paymentEvent: PaymentEvent): Long {
        return suspendTransaction(db = database) {
            PaymentEvents.insert {
                it[buyerId] = paymentEvent.buyerId
                it[orderId] = paymentEvent.orderId
                it[type] = paymentEvent.paymentType ?: PaymentType.NORMAL
                it[method] = paymentEvent.paymentMethod
                it[paymentKey] = paymentEvent.paymentKey
                it[isPaymentDone] = false
            }

            val eventId = PaymentEvents
                .select(PaymentEvents.id)
                .where(PaymentEvents.orderId eq paymentEvent.orderId)
                .single()[PaymentEvents.id]

            paymentEvent.paymentOrders.forEach { order ->
                PaymentOrders.insert {
                    it[paymentEventId] = eventId
                    it[sellerId] = order.sellerId
                    it[productId] = order.productId
                    it[orderId] = order.orderId
                    it[amount] = order.amount
                    it[paymentOrderStatus] = PaymentStatus.NOT_STARTED
                }
            }

            eventId
        }
    }

    override suspend fun getPendingPayments(): List<PendingPaymentEvent> {
        return suspendTransaction(db = database) {
            val threeMinutesAgo = JavaLocalDateTime.now().minusMinutes(3).toKotlinLocalDateTime()

            val results = (PaymentEvents innerJoin PaymentOrders)
                .select(
                    PaymentEvents.id,
                    PaymentEvents.paymentKey,
                    PaymentEvents.orderId,
                    PaymentOrders.id,
                    PaymentOrders.paymentOrderStatus,
                    PaymentOrders.amount,
                    PaymentOrders.failedCount,
                    PaymentOrders.threshold
                )
                .where {
                    ((PaymentOrders.paymentOrderStatus eq PaymentStatus.UNKNOWN) or
                            ((PaymentOrders.paymentOrderStatus eq PaymentStatus.EXECUTING) and
                                    (PaymentOrders.updatedAt lessEq threeMinutesAgo))) and
                            (PaymentOrders.failedCount less PaymentOrders.threshold)
                }
                .limit(10)
                .toList()

            results.groupBy { it[PaymentEvents.id] }
                .map { (eventId, rows) ->
                    PendingPaymentEvent(
                        paymentEventId = eventId,
                        paymentKey = rows.first()[PaymentEvents.paymentKey] ?: "",
                        orderId = rows.first()[PaymentEvents.orderId] ?: "",
                        pendingPaymentOrders = rows.map { row ->
                            PendingPaymentOrder(
                                paymentOrderId = row[PaymentOrders.id],
                                status = row[PaymentOrders.paymentOrderStatus],
                                amount = row[PaymentOrders.amount].toLong(),
                                failedCount = row[PaymentOrders.failedCount],
                                threshold = row[PaymentOrders.threshold]
                            )
                        }
                    )
                }
        }
    }


}