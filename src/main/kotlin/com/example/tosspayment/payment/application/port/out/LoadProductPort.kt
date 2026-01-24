package com.example.tosspayment.payment.application.port.out

import com.example.tosspayment.payment.domain.Product

interface LoadProductPort {

    suspend fun getProducts(cartId: Long, productIds: List<Long>): List<Product>
}