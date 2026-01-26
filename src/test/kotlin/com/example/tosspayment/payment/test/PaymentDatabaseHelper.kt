package com.example.tosspayment.payment.test

import com.example.tosspayment.payment.domain.PaymentEvent

interface PaymentDatabaseHelper {

  fun getPayments(orderId: String): PaymentEvent?

  suspend fun clean()
}