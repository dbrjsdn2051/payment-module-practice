package com.example.tosspayment.payment.adapter.out.web.product.client

import com.example.tosspayment.payment.domain.Product
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MockProductClient : ProductClient {

    override suspend fun getProducts(
        cartId: Long,
        productIds: List<Long>
    ): List<Product> {
        return productIds.map {
            Product(
                id = it,
                amount = BigDecimal(100),
                quantity = 2,
                name = "test_product_$it",
                sellerId = 1
            )
        }
    }
}