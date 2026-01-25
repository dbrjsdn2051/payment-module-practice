package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand

interface PaymentStatusUpdateRepository {

    suspend fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Boolean

    suspend fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Boolean
}