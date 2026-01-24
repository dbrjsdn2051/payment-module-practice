package com.example.tosspayment.payment.adapter.out.persistence

import com.example.tosspayment.payment.domain.PaymentEvent
import kotlinx.coroutines.flow.single
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.springframework.stereotype.Repository

@Repository
class R2DBCPaymentRepository(
    private val database: R2dbcDatabase
) : PaymentRepository {

    override suspend fun save(paymentEvent: PaymentEvent): Long {
        return suspendTransaction(db = database) {
            val result = PaymentEvents.insertReturning(listOf(PaymentEvents.id)) {
                it[buyerId] = paymentEvent.buyerId
                it[orderId] = paymentEvent.orderId
                it[type] = paymentEvent.paymentType ?: PaymentType.NORMAL
                it[method] = paymentEvent.paymentMethod
                it[paymentKey] = paymentEvent.paymentKey
                it[isPaymentDone] = false
            }

            val eventId = result.single()[PaymentEvents.id]

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
}
