package com.example.tosspayment.payment.adapter.out.persistence.exception

import com.example.tosspayment.payment.adapter.out.persistence.PaymentStatus

class PaymentAlreadyProcessedException(
    override val message: String,
    val status: PaymentStatus,
) : RuntimeException(message)