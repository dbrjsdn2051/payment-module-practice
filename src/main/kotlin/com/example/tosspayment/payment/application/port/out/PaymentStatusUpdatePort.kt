package com.example.tosspayment.payment.application.port.out

interface PaymentStatusUpdatePort {

    suspend fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Boolean
}