package com.example.tosspayment.payment.application.port.`in`

import com.example.tosspayment.payment.domain.CheckoutResult

interface CheckoutUseCase {

    suspend fun checkout(command: CheckoutCommand): CheckoutResult
}