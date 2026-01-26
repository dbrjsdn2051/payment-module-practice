package com.example.tosspayment.payment.adapter.out.persistence

import com.example.tosspayment.common.PersistentAdapter
import com.example.tosspayment.payment.adapter.out.persistence.repository.PaymentOutboxRepository
import com.example.tosspayment.payment.application.port.out.LoadPendingPaymentEventMessagePort
import com.example.tosspayment.payment.domain.PaymentEventMessage

@PersistentAdapter
class PaymentEventMessagePersistenceAdapter(
    private val paymentOutboxRepository: PaymentOutboxRepository
) : LoadPendingPaymentEventMessagePort {

    override suspend fun getPendingPaymentEventMessage(): List<PaymentEventMessage> {
        return paymentOutboxRepository.getPendingPaymentOutboxes()
    }
}
