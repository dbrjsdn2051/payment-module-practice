package com.example.tosspayment.payment.adapter.out.persistence.repository

import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.domain.PaymentEventMessage
import com.example.tosspayment.payment.domain.PaymentEventMessageType
import reactor.core.publisher.Mono

interface PaymentOutboxRepository {

    suspend fun insertOutbox(command: PaymentStatusUpdateCommand): PaymentEventMessage

    fun markMessageAsSent(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>

    fun markMessageAsFailure(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>

    suspend fun getPendingPaymentOutboxes(): List<PaymentEventMessage>
}
