package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.payment.domain.PaymentEvent

interface PaymentRepository {

    suspend fun save(paymentEvent: PaymentEvent): Long
}