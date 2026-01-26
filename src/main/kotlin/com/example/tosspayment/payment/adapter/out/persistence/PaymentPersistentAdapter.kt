package com.example.tosspayment.payment.adapter.out.persistence

import com.example.tosspayment.common.PersistentAdapter
import com.example.tosspayment.payment.adapter.out.persistence.repository.PaymentRepository
import com.example.tosspayment.payment.adapter.out.persistence.repository.PaymentStatusUpdateRepository
import com.example.tosspayment.payment.adapter.out.persistence.repository.PaymentValidationRepository
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.application.port.out.LoadPendingPaymentPort
import com.example.tosspayment.payment.application.port.out.PaymentStatusUpdatePort
import com.example.tosspayment.payment.application.port.out.PaymentValidationPort
import com.example.tosspayment.payment.application.port.out.SavePaymentPort
import com.example.tosspayment.payment.domain.PaymentEvent
import com.example.tosspayment.payment.domain.PendingPaymentEvent

@PersistentAdapter
class PaymentPersistentAdapter(
    private val paymentRepository: PaymentRepository,
    private val paymentStatusUpdateRepository: PaymentStatusUpdateRepository,
    private val paymentValidationRepository: PaymentValidationRepository
) : SavePaymentPort, PaymentStatusUpdatePort, PaymentValidationPort, LoadPendingPaymentPort {

    override suspend fun save(paymentEvent: PaymentEvent): Long {
        return paymentRepository.save(paymentEvent)
    }

    override suspend fun updatePaymentStatusToExecuting(
        orderId: String,
        paymentKey: String
    ): Boolean {
        return paymentStatusUpdateRepository.updatePaymentStatusToExecuting(orderId, paymentKey)
    }

    override suspend fun isValid(orderId: String, amount: Long) {
        paymentValidationRepository.isValid(orderId, amount)
    }

    override suspend fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Boolean {
        return paymentStatusUpdateRepository.updatePaymentStatus(command)
    }

    override suspend fun getPendingPayments(): List<PendingPaymentEvent> {
        return paymentRepository.getPendingPayments()
    }
}