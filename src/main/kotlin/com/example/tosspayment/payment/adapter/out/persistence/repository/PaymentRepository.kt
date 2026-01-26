package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.payment.domain.PaymentEvent
import com.example.tosspayment.payment.domain.PendingPaymentEvent

interface PaymentRepository {

    suspend fun save(paymentEvent: PaymentEvent): Long

    suspend fun getPendingPayments(): List<PendingPaymentEvent>
}