package com.example.tosspayment.payment.adapter.out.persistence.repository

interface PaymentValidationRepository {

    suspend fun isValid(orderId: String, amount: Long)
}