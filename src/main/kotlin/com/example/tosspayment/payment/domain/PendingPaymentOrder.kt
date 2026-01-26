package com.example.tosspayment.payment.domain

import com.example.tosspayment.payment.adapter.out.persistence.PaymentStatus

data class PendingPaymentOrder(
    val paymentOrderId: Long,
    val status: PaymentStatus,
    val amount: Long,
    val failedCount: Int,
    val threshold: Int,
)
