package com.example.tosspayment.payment.application.service

import com.example.tosspayment.payment.adapter.out.persistence.PaymentStatus
import com.example.tosspayment.payment.adapter.out.persistence.exception.PaymentAlreadyProcessedException
import com.example.tosspayment.payment.adapter.out.persistence.exception.PaymentValidationException
import com.example.tosspayment.payment.adapter.out.web.exception.PSPConfirmationException
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.application.port.out.PaymentStatusUpdatePort
import com.example.tosspayment.payment.domain.PaymentConfirmationResult
import com.example.tosspayment.payment.domain.PaymentFailure
import io.netty.handler.timeout.TimeoutException
import org.springframework.stereotype.Service

@Service
class PaymentErrorHandler(
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort
) {

    suspend fun handlePaymentConfirmationError(
        error: Throwable,
        command: PaymentConfirmCommand
    ): PaymentConfirmationResult {
        val (status, failure) = when (error) {
            is PSPConfirmationException -> Pair(
                error.paymentStatus(),
                PaymentFailure(error.errorCode, error.errorMessage)
            )
            is PaymentValidationException -> Pair(
                PaymentStatus.FAILURE,
                PaymentFailure(error::class.simpleName ?: "", error.message ?: "")
            )
            is PaymentAlreadyProcessedException -> return PaymentConfirmationResult(
                status = error.status,
                failure = PaymentFailure(error::class.simpleName ?: "", error.message ?: "")
            )
            is TimeoutException -> Pair(
                PaymentStatus.UNKNOWN,
                PaymentFailure(error::class.simpleName ?: "", error.message ?: "")
            )
            else -> Pair(
                PaymentStatus.UNKNOWN,
                PaymentFailure(error::class.simpleName ?: "", error.message ?: "")
            )
        }

        val paymentStatusUpdateCommand = PaymentStatusUpdateCommand(
            paymentKey = command.paymentKey,
            orderId = command.orderId,
            status = status,
            failure = failure
        )

        paymentStatusUpdatePort.updatePaymentStatus(paymentStatusUpdateCommand)
        return PaymentConfirmationResult(status, failure)
    }
}
