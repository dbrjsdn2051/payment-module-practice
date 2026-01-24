package com.example.tosspayment.payment.adapter.out.persistence

import com.example.tosspayment.common.PersistentAdapter
import com.example.tosspayment.payment.application.port.out.SavePaymentPort
import com.example.tosspayment.payment.domain.PaymentEvent

@PersistentAdapter
class PaymentPersistentAdapter(
    private val paymentRepository: PaymentRepository,
) : SavePaymentPort {

    override suspend fun save(paymentEvent: PaymentEvent): Long {
        return paymentRepository.save(paymentEvent)
    }
}