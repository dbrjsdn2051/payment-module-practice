package com.example.tosspayment.payment.adapter.out.stream

import com.example.tosspayment.payment.domain.PaymentEventMessage
import com.example.tosspayment.payment.domain.PaymentEventMessageType
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.TestPropertySource
import java.util.UUID

@SpringBootTest
@Tag("ExternalIntegration")
@EmbeddedKafka(
    partitions = 6,
    topics = ["payment"]
)
@TestPropertySource(properties = [
    "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
    "spring.cloud.stream.kafka.binder.brokers=\${spring.embedded.kafka.brokers}"
])
class PaymentEventMessageSenderTest(
    @Autowired private val paymentEventMessageSender: PaymentEventMessageSender
) {

    @Test
    fun `should send eventMessage by using partitionKey`() {
        val paymentEventMessages = (0..5).map { partitionKey ->
            PaymentEventMessage(
                type = PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS,
                payload = mapOf("orderId" to UUID.randomUUID().toString()),
                metadata = mapOf("partitionKey" to partitionKey)
            )
        }

        paymentEventMessages.forEach {
            paymentEventMessageSender.dispatch(it)
        }

        Thread.sleep(5000)
    }
}
