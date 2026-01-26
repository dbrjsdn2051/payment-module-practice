package com.example.tosspayment.payment.adapter.out.web.toss.executor

import com.example.tosspayment.payment.adapter.out.web.exception.PSPConfirmationException
import com.example.tosspayment.payment.adapter.out.web.exception.TossPaymentError
import com.example.tosspayment.payment.adapter.out.web.executor.TossPaymentExecutor
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.test.PSPTestWebClientConfiguration
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import tools.jackson.databind.ObjectMapper
import java.util.*

@SpringBootTest
@Import(PSPTestWebClientConfiguration::class)
@Tag("TooLongTime")
class TossPaymentExecutorTest(
    @Autowired private val pspTestWebClientConfiguration: PSPTestWebClientConfiguration,
    @Autowired private val objectMapper: ObjectMapper
) {

    @Test
    fun `should handle failure error - REJECT_CARD_COMPANY`() = runTest {
        val errorCode = TossPaymentError.REJECT_CARD_COMPANY
        val command = createCommand()

        val paymentExecutor = createPaymentExecutor(errorCode.name)

        try {
            paymentExecutor.execute(command)
        } catch (e: PSPConfirmationException) {
            assertThat(e.isSuccess).isFalse()
            assertThat(e.isFailure).isTrue()
            assertThat(e.isUnknown).isFalse()
            assertThat(e.errorCode).isEqualTo(errorCode.name)
        }
    }

    @Test
    fun `should handle failure error - EXCEED_MAX_CARD_INSTALLMENT_PLAN`() = runTest {
        val errorCode = TossPaymentError.EXCEED_MAX_CARD_INSTALLMENT_PLAN
        val command = createCommand()

        val paymentExecutor = createPaymentExecutor(errorCode.name)

        try {
            paymentExecutor.execute(command)
        } catch (e: PSPConfirmationException) {
            assertThat(e.isSuccess).isFalse()
            assertThat(e.isFailure).isTrue()
            assertThat(e.isUnknown).isFalse()
            assertThat(e.errorCode).isEqualTo(errorCode.name)
        }
    }

    @Test
    fun `should handle unknown error with retry - PROVIDER_ERROR`() = runTest {
        val errorCode = TossPaymentError.PROVIDER_ERROR
        val command = createCommand()

        val paymentExecutor = createPaymentExecutor(errorCode.name)

        try {
            paymentExecutor.execute(command)
        } catch (e: PSPConfirmationException) {
            // 재시도 후에도 실패하면 Unknown 상태로 처리
            assertThat(e.isSuccess).isFalse()
            assertThat(e.isFailure).isFalse()
            assertThat(e.isUnknown).isTrue()
            assertThat(e.isRetryableError).isTrue()
        }
    }

    @Test
    fun `should handle success error - ALREADY_PROCESSED_PAYMENT`() = runTest {
        val errorCode = TossPaymentError.ALREADY_PROCESSED_PAYMENT
        val command = createCommand()

        val paymentExecutor = createPaymentExecutor(errorCode.name)

        try {
            paymentExecutor.execute(command)
        } catch (e: PSPConfirmationException) {
            assertThat(e.isSuccess).isTrue()
            assertThat(e.isFailure).isFalse()
            assertThat(e.isUnknown).isFalse()
        }
    }

    private fun createCommand(): PaymentConfirmCommand {
        return PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = UUID.randomUUID().toString(),
            amount = 10000L
        )
    }

    private fun createPaymentExecutor(errorCode: String): TossPaymentExecutor {
        return TossPaymentExecutor(
            tossPaymentWebClient = pspTestWebClientConfiguration.createTestTossWebClient(
                Pair("TossPayments-Test-Code", errorCode)
            ),
            objectMapper = objectMapper,
            uri = "/v1/payments/confirm"
        )
    }
}
