package com.example.tosspayment.payment.application.port.out

interface PaymentValidationPort {

    suspend fun isValid(orderId: String, amount: Long)
}