package com.example.tosspayment.payment.adapter.out.web

import com.example.tosspayment.common.WebAdapter
import com.example.tosspayment.payment.adapter.out.web.executor.PaymentExecutor
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.application.port.out.PaymentExecutorPort
import com.example.tosspayment.payment.domain.PaymentExecutionResult

@WebAdapter
class PaymentExecutorWebAdapter(
    private val paymentExecutor: PaymentExecutor
) : PaymentExecutorPort {

    override suspend fun execute(command: PaymentConfirmCommand): PaymentExecutionResult {
        return paymentExecutor.execute(command)
    }
}