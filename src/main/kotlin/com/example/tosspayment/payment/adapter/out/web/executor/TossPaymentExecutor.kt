package com.example.tosspayment.payment.adapter.out.web.executor

import com.example.tosspayment.payment.adapter.out.persistence.PaymentMethod
import com.example.tosspayment.payment.adapter.out.persistence.PaymentType
import com.example.tosspayment.payment.adapter.out.web.response.TossPaymentConfirmationResponse
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.domain.PSPConfirmationStatus
import com.example.tosspayment.payment.domain.PaymentExecutionResult
import com.example.tosspayment.payment.domain.PaymentExtraDetails
import tools.jackson.databind.ObjectMapper
import kotlinx.datetime.toKotlinLocalDateTime
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class TossPaymentExecutor(
    private val tossPaymentWebClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val uri: String = "/v1/payments/confirm"
) : PaymentExecutor {

    override suspend fun execute(command: PaymentConfirmCommand): PaymentExecutionResult {
        val response = tossPaymentWebClient.post()
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
            .retrieve()
            .awaitBody<TossPaymentConfirmationResponse>()

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
