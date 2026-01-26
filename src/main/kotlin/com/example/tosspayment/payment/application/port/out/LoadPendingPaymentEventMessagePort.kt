package com.example.tosspayment.payment.application.port.out

import com.example.tosspayment.payment.domain.PaymentEventMessage

interface LoadPendingPaymentEventMessagePort {

    suspend fun getPendingPaymentEventMessage(): List<PaymentEventMessage>
}
