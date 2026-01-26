package com.example.tosspayment.payment.adapter.out.web.executor

import com.example.tosspayment.payment.domain.PaymentMethod
import com.example.tosspayment.payment.domain.PaymentType
import com.example.tosspayment.payment.adapter.out.web.exception.PSPConfirmationException
import com.example.tosspayment.payment.adapter.out.web.exception.TossPaymentError
import com.example.tosspayment.payment.adapter.out.web.response.TossPaymentConfirmationResponse
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.domain.PSPConfirmationStatus
import com.example.tosspayment.payment.domain.PaymentExecutionResult
import com.example.tosspayment.payment.domain.PaymentExtraDetails
import io.netty.handler.timeout.TimeoutException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.datetime.toKotlinLocalDateTime
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.util.retry.Retry
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Component
class TossPaymentExecutor(
    private val tossPaymentWebClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val uri: String = "/v1/payments/confirm"
) : PaymentExecutor {

    override suspend fun execute(command: PaymentConfirmCommand): PaymentExecutionResult {
        return mono {
            tossPaymentWebClient.post()
                .uri(uri)
                .header("Idempotency-Key", command.orderId)
                .bodyValue(
                    """
                    {
                      "paymentKey": "${command.paymentKey}",
                      "orderId": "${command.orderId}",
                      "amount": ${command.amount}
                    }
                    """.trimIndent()
                )
                .awaitExchange { response ->
                    if (response.statusCode().is2xxSuccessful) {
                        val successResponse = response.awaitBody<TossPaymentConfirmationResponse>()
                        createSuccessResult(command, successResponse)
                    } else {
                        val failureResponse = response.awaitBody<TossPaymentConfirmationResponse.TossFailureResponse>()
                        val error = TossPaymentError.get(failureResponse.code ?: "UNKNOWN")

                        throw PSPConfirmationException(
                            errorCode = error.name,
                            errorMessage = error.description,
                            isSuccess = error.isSuccess(),
                            isFailure = error.isFailure(),
                            isUnknown = error.isUnknown(),
                            isRetryableError = error.isRetryableError()
                        )
                    }
                }
        }
        .retryWhen(
            Retry.backoff(2, Duration.ofSeconds(1))
                .jitter(0.1)
                .filter { (it is PSPConfirmationException && it.isRetryableError) || it is TimeoutException }
                .onRetryExhaustedThrow { _, retrySignal -> retrySignal.failure() }
        )
        .awaitSingle()
    }

    private fun createSuccessResult(
        command: PaymentConfirmCommand,
        response: TossPaymentConfirmationResponse
    ): PaymentExecutionResult {

        return PaymentExecutionResult(
            paymentKey = command.paymentKey,
            orderId = command.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.get(response.type),
                method = PaymentMethod.get(response.method ?: ""),
                approvedAt = response.approvedAt!!.toLocalDateTime().toKotlinLocalDateTime(),
                orderName = response.orderName,
                pspConfirmationStatus = PSPConfirmationStatus.get(response.status),
                totalAmount = response.totalAmount,
                pspRawData = objectMapper.writeValueAsString(response)
            ),
            isSuccess = true,
            isFailure = false,
            isUnknown = false,
            isRetryable = false
        )
    }

}
