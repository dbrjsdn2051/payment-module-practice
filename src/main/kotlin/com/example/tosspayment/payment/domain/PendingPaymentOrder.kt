package com.example.tosspayment.payment.domain

data class PendingPaymentOrder(
    val paymentOrderId: Long,
    val status: PaymentStatus,
    val amount: Long,
    val failedCount: Int,
    val threshold: Int,
)
