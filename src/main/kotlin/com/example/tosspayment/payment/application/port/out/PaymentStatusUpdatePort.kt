package com.example.tosspayment.payment.application.port.out

import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand

interface PaymentStatusUpdatePort {

    suspend fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Boolean

    suspend fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Boolean
}