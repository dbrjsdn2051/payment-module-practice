package com.example.tosspayment.payment.application.port.out

import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.domain.PaymentExecutionResult

interface PaymentExecutorPort {

    suspend fun execute(command: PaymentConfirmCommand): PaymentExecutionResult
}