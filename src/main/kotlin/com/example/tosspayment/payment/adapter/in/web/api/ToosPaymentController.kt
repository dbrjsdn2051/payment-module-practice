package com.example.tosspayment.payment.adapter.`in`.web.api

import com.example.tosspayment.common.WebAdapter
import com.example.tosspayment.payment.adapter.`in`.web.request.TossPaymentConfirmRequest
import com.example.tosspayment.payment.adapter.`in`.web.response.ApiResponse
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmUseCase
import com.example.tosspayment.payment.domain.PaymentConfirmationResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@WebAdapter
@RestController
@RequestMapping("/v1/toss")
class ToosPaymentController(
    private val paymentConfirmUseCase: PaymentConfirmUseCase,
) {

    @PostMapping("/confirm")
    suspend fun confirm(
        @RequestBody request: TossPaymentConfirmRequest
    ): ResponseEntity<ApiResponse<PaymentConfirmationResult>> {
        val command = PaymentConfirmCommand(
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            amount = request.amount.toLong()
        )

        val result = paymentConfirmUseCase.confirm(command)
        return ResponseEntity.ok().body(ApiResponse.with(HttpStatus.OK, "", result))
    }
}