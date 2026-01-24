package com.example.tosspayment.payment.adapter.out.web.product

import com.example.tosspayment.common.WebAdapter
import com.example.tosspayment.payment.adapter.out.web.product.client.ProductClient
import com.example.tosspayment.payment.application.port.out.LoadProductPort
import com.example.tosspayment.payment.domain.Product

@WebAdapter
class ProductWebAdapter(
    private val productClient: ProductClient
) : LoadProductPort {

    override suspend fun getProducts(
        cartId: Long,
        productIds: List<Long>
    ): List<Product> {
        return productClient.getProducts(cartId, productIds)
    }
}