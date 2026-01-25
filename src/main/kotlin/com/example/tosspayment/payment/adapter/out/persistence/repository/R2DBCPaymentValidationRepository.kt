package com.example.tosspayment.payment.adapter.out.persistence.repository

import org.springframework.stereotype.Repository

@Repository
class R2DBCPaymentValidationRepository : PaymentValidationRepository {

    override suspend fun isValid(orderId: String, amount: Long) {
        // TODO: 검증 로직 구현 (실패 시 예외 throw)
    }
}