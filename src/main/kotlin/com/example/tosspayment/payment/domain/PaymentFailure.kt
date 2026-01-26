package com.example.tosspayment.payment.domain

data class PaymentFailure(
    val errorCode: String,
    val message: String
)