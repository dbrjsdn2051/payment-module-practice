package com.example.tosspayment.payment.application.port.`in`

import com.example.tosspayment.payment.domain.PaymentConfirmationResult

interface PaymentConfirmUseCase {

    suspend fun confirm(command: PaymentConfirmCommand): PaymentConfirmationResult
}