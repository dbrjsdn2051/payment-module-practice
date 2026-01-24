package com.example.tosspayment.payment.application.port.out

import com.example.tosspayment.payment.domain.PaymentEvent

interface SavePaymentPort {
    suspend fun save(paymentEvent: PaymentEvent): Long
}