package com.example.tosspayment.payment.domain

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalEventPublisher

@Component
class PaymentEventMessagePublisher(
    publisher: ApplicationEventPublisher
) {
    private val transactionalEventPublisher = TransactionalEventPublisher(publisher)

    fun publishEvent(paymentEventMessage: PaymentEventMessage): PaymentEventMessage {
        return paymentEventMessage.also {
            transactionalEventPublisher.publishEvent(it)
        }
    }
}