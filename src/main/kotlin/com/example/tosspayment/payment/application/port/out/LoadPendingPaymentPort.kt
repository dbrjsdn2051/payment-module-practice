package com.example.tosspayment.payment.application.port.out

import com.example.tosspayment.payment.domain.PendingPaymentEvent

interface LoadPendingPaymentPort {

    suspend fun getPendingPayments(): List<PendingPaymentEvent>
}