package com.example.tosspayment.payment.adapter.out.stream

import com.example.tosspayment.common.Logger
import com.example.tosspayment.common.StreamAdapter
import com.example.tosspayment.payment.application.port.out.DispatchEventMessagePort
import com.example.tosspayment.payment.domain.PaymentEventMessage
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.integration.IntegrationMessageHeaderAccessor
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@StreamAdapter
class PaymentEventMessageSender(
    private val streamBridge: StreamBridge
) : DispatchEventMessagePort {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun dispatchAfterCommit(paymentEventMessage: PaymentEventMessage) {
        Logger.info("dispatchAfterCommit", "Dispatching event message after commit", paymentEventMessage.payload["orderId"])
        dispatch(paymentEventMessage)
    }

    override fun dispatch(paymentEventMessage: PaymentEventMessage) {
        Logger.info("dispatch", "Dispatching event message", paymentEventMessage.payload["orderId"])

        val message = MessageBuilder
            .withPayload(paymentEventMessage)
            .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, paymentEventMessage.payload["orderId"])
            .setHeader(KafkaHeaders.PARTITION, paymentEventMessage.metadata["partitionKey"] ?: 0)
            .build()

        val result = streamBridge.send("send-out-0", message)

        Logger.info("dispatch", "Event message sent", mapOf("orderId" to paymentEventMessage.payload["orderId"], "result" to result))
    }
}
