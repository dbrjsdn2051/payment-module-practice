package com.example.tosspayment.payment.adapter.out.web.executor

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class TossPaymentExecutor(
    private val tossPaymentWebClient: WebClient,
    private val uri: String = "/v1/payments/confirm"
) {

    suspend fun execute(paymentKey: String, orderId: String, amount: String): String {
        return tossPaymentWebClient.post()
            .uri(uri)
            .bodyValue(
                """
                {
                  "paymentKey": "$paymentKey",
                  "orderId": "$orderId",
                  "amount": $amount
                }
            """.trimIndent()
            )
            .retrieve()
            .awaitBody()
    }
}