package com.example.tosspayment.payment.test

import com.example.tosspayment.payment.adapter.out.persistence.PaymentEvents
import com.example.tosspayment.payment.adapter.out.persistence.PaymentOrderHistories
import com.example.tosspayment.payment.adapter.out.persistence.PaymentOrders
import com.example.tosspayment.payment.domain.PaymentEvent
import com.example.tosspayment.payment.domain.PaymentOrder
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteAll
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.springframework.stereotype.Component

@Component
class R2DBCPaymentDatabaseHelper(
    private val database: R2dbcDatabase
) : PaymentDatabaseHelper {

    override fun getPayments(orderId: String): PaymentEvent? {
        return null
    }

    override suspend fun clean() {
        suspendTransaction(db = database) {
            PaymentOrderHistories.deleteAll()
            PaymentOrders.deleteAll()
            PaymentEvents.deleteAll()
        }
    }
}
