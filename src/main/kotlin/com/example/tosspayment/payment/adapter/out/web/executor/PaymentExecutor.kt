package com.example.tosspayment.payment.adapter.out.web.executor

import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.domain.PaymentExecutionResult

interface PaymentExecutor {

    suspend fun execute(command: PaymentConfirmCommand): PaymentExecutionResult
}