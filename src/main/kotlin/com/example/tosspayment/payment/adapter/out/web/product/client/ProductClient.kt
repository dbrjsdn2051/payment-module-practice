package com.example.tosspayment.payment.adapter.out.web.product.client

import com.example.tosspayment.payment.domain.Product

interface ProductClient {

    suspend fun getProducts(cartId: Long, productIds: List<Long>): List<Product>
}