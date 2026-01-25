package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.payment.adapter.out.persistence.PaymentOrders
import com.example.tosspayment.payment.adapter.out.persistence.exception.PaymentValidationException
import kotlinx.coroutines.flow.single
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.springframework.stereotype.Repository

@Repository
class R2DBCPaymentValidationRepository(
    private val database: R2dbcDatabase
) : PaymentValidationRepository {

    override suspend fun isValid(orderId: String, amount: Long) {
        suspendTransaction(db = database) {
            val sumColumn = PaymentOrders.amount.sum()
            val totalAmount = PaymentOrders.select(sumColumn)
                .where(PaymentOrders.orderId eq orderId)
                .single()[sumColumn]
                ?.toLong() ?: 0L

            if (totalAmount != amount) {
                throw PaymentValidationException("결제 (orderId: $orderId) 에서 금액이 올바르지 않습니다. 예상: $totalAmount, 실제: $amount")
            }
            true
        }
    }
}