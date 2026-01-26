package com.example.tosspayment.payment.application.service

import com.example.tosspayment.payment.adapter.out.persistence.repository.PaymentOutboxRepository
import com.example.tosspayment.payment.application.port.`in`.PaymentEventMessageRelayUseCase
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.application.port.out.DispatchEventMessagePort
import com.example.tosspayment.payment.application.port.out.LoadPendingPaymentEventMessagePort
import com.example.tosspayment.payment.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.TestPropertySource
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.util.*

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
class PaymentEventMessageRelayServiceTest(
    @Autowired private val paymentOutboxRepository: PaymentOutboxRepository,
    @Autowired private val loadPendingPaymentEventMessagePort: LoadPendingPaymentEventMessagePort,
    @Autowired private val dispatchEventMessagePort: DispatchEventMessagePort
) {

    @Test
    fun `should dispatch external message system`() = runBlocking {
        val paymentEventMessageRelayUseCase: PaymentEventMessageRelayUseCase =
            PaymentEventMessageRelayService(loadPendingPaymentEventMessagePort, dispatchEventMessagePort)

        val command = PaymentStatusUpdateCommand(
            paymentExecutionResult = PaymentExecutionResult(
                paymentKey = UUID.randomUUID().toString(),
                orderId = UUID.randomUUID().toString(),
                extraDetails = PaymentExtraDetails(
                    type = PaymentType.NORMAL,
                    method = PaymentMethod.EASY_PAY,
                    approvedAt = LocalDateTime.now().toKotlinLocalDateTime(),
                    orderName = "test_order_name",
                    pspConfirmationStatus = PSPConfirmationStatus.DONE,
                    totalAmount = 50000L,
                    pspRawData = "{}"
                ),
                isSuccess = true,
                isFailure = false,
                isUnknown = false,
                isRetryable = false
            )
        )

        paymentOutboxRepository.insertOutbox(command)

        paymentEventMessageRelayUseCase.relay()

        Thread.sleep(10000)
    }
}
