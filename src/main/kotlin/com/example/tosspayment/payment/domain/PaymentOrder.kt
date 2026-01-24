package com.example.tosspayment.payment.domain

import com.example.tosspayment.payment.adapter.out.persistence.PaymentStatus
import java.math.BigDecimal

data class PaymentOrder(
    val id: Long? = null,
    val paymentEventId: Long? = null,
    val sellerId: Long,
    val buyerId: Long? = null,
    val productId: Long,
    val orderId: String,
    val amount: BigDecimal,
    val paymentStatus: PaymentStatus,
    private val isLedgerUpdated: Boolean = false,
    private val isWalletUpdated: Boolean = false,
) {
}