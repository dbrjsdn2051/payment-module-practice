package com.example.tosspayment.payment.application.port.out

import com.example.tosspayment.payment.domain.PaymentEventMessage

interface DispatchEventMessagePort {

    fun dispatch(paymentEventMessage: PaymentEventMessage)
}
