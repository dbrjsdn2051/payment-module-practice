package com.example.tosspayment.payment.adapter.out.persistence.repository

interface PaymentStatusUpdateRepository {

    suspend fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Boolean
}