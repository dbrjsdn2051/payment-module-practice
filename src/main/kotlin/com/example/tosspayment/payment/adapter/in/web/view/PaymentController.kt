package com.example.tosspayment.payment.adapter.`in`.web.view

import com.example.tosspayment.common.WebAdapter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
@WebAdapter
class PaymentController {

    @GetMapping("/v1/toss/success")
    fun successPage() : Mono<String> {
        return Mono.just("success")
    }

    @GetMapping("/v1/toss/fail")
    fun failPage() : Mono<String> {
        return Mono.just("fail")
    }
}