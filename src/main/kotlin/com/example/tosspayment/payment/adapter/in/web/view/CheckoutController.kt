package com.example.tosspayment.payment.adapter.`in`.web.view

import com.example.tosspayment.common.IdempotencyCreator
import com.example.tosspayment.common.WebAdapter
import com.example.tosspayment.payment.adapter.`in`.web.request.CheckoutRequest
import com.example.tosspayment.payment.application.port.`in`.CheckoutCommand
import com.example.tosspayment.payment.application.port.`in`.CheckoutUseCase
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@WebAdapter
@Controller
class CheckoutController(private val checkoutUseCase: CheckoutUseCase) {

    @GetMapping("/")
    suspend fun checkoutPage(request: CheckoutRequest, model: Model): String {
        val command = CheckoutCommand(
            cartId = request.cartId,
            buyerId = request.buyerId,
            productIds = request.productIds,
            idempotencyKey = IdempotencyCreator.create(request.seed)
        )

        val result = checkoutUseCase.checkout(command)

        model.addAttribute("orderId", result.orderId)
        model.addAttribute("orderName", result.orderName)
        model.addAttribute("amount", result.amount)

        return "checkout"
    }
}