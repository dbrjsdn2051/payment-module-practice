package com.example.tosspayment.payment.application.service

import com.example.tosspayment.common.UseCase
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmUseCase
import com.example.tosspayment.payment.application.port.out.PaymentStatusUpdatePort
import com.example.tosspayment.payment.application.port.out.PaymentValidationPort
import com.example.tosspayment.payment.domain.PaymentConfirmationResult

@UseCase
class PaymentConfirmService(
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    private val paymentValidationPort: PaymentValidationPort
) : PaymentConfirmUseCase {

    override suspend fun confirm(command: PaymentConfirmCommand): PaymentConfirmationResult {
        // 1. 상태 업데이트 (EXECUTING으로 변경)
        paymentStatusUpdatePort.updatePaymentStatusToExecuting(command.orderId, command.paymentKey)

        // 2. 검증 (실패 시 예외 발생)
        paymentValidationPort.isValid(command.orderId, command.amount)

        // 3. 결과 반환
        return PaymentConfirmationResult()
    }
}