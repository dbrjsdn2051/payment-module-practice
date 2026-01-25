package com.example.tosspayment.payment.adapter.`in`.web.view

import com.example.tosspayment.common.WebAdapter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
@WebAdapter
class PaymentController {

    @GetMapping("/v1/toss/success")
    fun successPage(): String {
        return "success"
    }

    @GetMapping("/v1/toss/fail")
    fun failPage(): String {
        return "fail"
    }
}