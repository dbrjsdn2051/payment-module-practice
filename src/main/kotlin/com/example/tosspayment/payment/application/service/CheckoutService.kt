package com.example.tosspayment.payment.application.service

import com.example.tosspayment.common.UseCase
import com.example.tosspayment.payment.domain.PaymentStatus
import com.example.tosspayment.payment.application.port.`in`.CheckoutCommand
import com.example.tosspayment.payment.application.port.`in`.CheckoutUseCase
import com.example.tosspayment.payment.application.port.out.LoadProductPort
import com.example.tosspayment.payment.application.port.out.SavePaymentPort
import com.example.tosspayment.payment.domain.CheckoutResult
import com.example.tosspayment.payment.domain.PaymentEvent
import com.example.tosspayment.payment.domain.PaymentOrder
import com.example.tosspayment.payment.domain.Product

@UseCase
class CheckoutService(
    private val loadProductPort: LoadProductPort,
    private val savePaymentPort: SavePaymentPort
) : CheckoutUseCase {

    override suspend fun checkout(command: CheckoutCommand): CheckoutResult {
        val products = loadProductPort.getProducts(command.cartId, command.productIds)

        val paymentEvent = createPaymentEvent(command, products)
        savePaymentPort.save(paymentEvent)

        return CheckoutResult(
            amount = paymentEvent.totalAmount(),
            orderId = paymentEvent.orderId,
            orderName = paymentEvent.orderName
        )
    }

    private fun createPaymentEvent(command: CheckoutCommand, products: List<Product>): PaymentEvent {
        return PaymentEvent(
            buyerId = command.buyerId,
            orderId = command.idempotencyKey,
            orderName = products.joinToString { it.name },
            paymentOrders = products.map {
                PaymentOrder(
                    sellerId = it.sellerId,
                    orderId = command.idempotencyKey,
                    productId = it.id,
                    amount = it.amount,
                    paymentStatus = PaymentStatus.NOT_STARTED
                )
            }
        )
    }
}