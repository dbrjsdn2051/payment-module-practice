package com.example.tosspayment.payment.application.service

import com.example.tosspayment.payment.adapter.out.persistence.PaymentMethod
import com.example.tosspayment.payment.adapter.out.persistence.PaymentStatus
import com.example.tosspayment.payment.adapter.out.persistence.PaymentType
import com.example.tosspayment.payment.adapter.out.web.exception.PSPConfirmationException
import com.example.tosspayment.payment.application.port.`in`.CheckoutCommand
import com.example.tosspayment.payment.application.port.`in`.CheckoutUseCase
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.application.port.out.LoadPendingPaymentPort
import com.example.tosspayment.payment.application.port.out.PaymentExecutorPort
import com.example.tosspayment.payment.application.port.out.PaymentStatusUpdatePort
import com.example.tosspayment.payment.application.port.out.PaymentValidationPort
import com.example.tosspayment.payment.domain.PSPConfirmationStatus
import com.example.tosspayment.payment.domain.PaymentExecutionResult
import com.example.tosspayment.payment.domain.PaymentExtraDetails
import com.example.tosspayment.payment.domain.PaymentFailure
import com.example.tosspayment.payment.test.PaymentDatabaseHelper
import com.example.tosspayment.payment.test.PaymentTestConfiguration
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@Import(PaymentTestConfiguration::class)
class PaymentRecoveryServiceTest(
    @Autowired private val loadPendingPaymentPort: LoadPendingPaymentPort,
    @Autowired private val paymentValidationPort: PaymentValidationPort,
    @Autowired private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    @Autowired private val checkoutUseCase: CheckoutUseCase,
    @Autowired private val paymentDatabaseHelper: PaymentDatabaseHelper,
    @Autowired private val paymentErrorHandler: PaymentErrorHandler
) {

    @BeforeEach
    fun clean() = runTest {
        paymentDatabaseHelper.clean()
    }

    @Test
    fun `should recovery payments`() = runTest {
        val paymentConfirmCommand = createUnknownStatusPaymentEvent()
        val paymentExecutionResult = createPaymentExecutionResult(paymentConfirmCommand)

        val mockPaymentExecutorPort = mockk<PaymentExecutorPort>()

        coEvery { mockPaymentExecutorPort.execute(any()) } returns paymentExecutionResult

        val paymentRecoveryService = PaymentRecoveryService(
            loadPendingPaymentPort = loadPendingPaymentPort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutorPort,
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentErrorHandler = paymentErrorHandler
        )

        paymentRecoveryService.recovery()

        delay(3000)
    }

    @Test
    fun `should fail to recovery payment when an unknown exception occurs`() = runTest {
        val paymentConfirmCommand = createUnknownStatusPaymentEvent()

        val mockPaymentExecutorPort = mockk<PaymentExecutorPort>()

        coEvery { mockPaymentExecutorPort.execute(any()) } throws PSPConfirmationException(
            errorCode = "UNKNOWN_ERROR",
            errorMessage = "test_error_message",
            isSuccess = false,
            isFailure = false,
            isUnknown = true,
            isRetryableError = true
        )

        val paymentRecoveryService = PaymentRecoveryService(
            loadPendingPaymentPort = loadPendingPaymentPort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutorPort,
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentErrorHandler = paymentErrorHandler
        )

        paymentRecoveryService.recovery()

        delay(1000)
    }

    private suspend fun createUnknownStatusPaymentEvent(): PaymentConfirmCommand {
        val orderId = UUID.randomUUID().toString()
        val paymentKey = UUID.randomUUID().toString()

        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2),
            idempotencyKey = orderId
        )

        val checkoutResult = checkoutUseCase.checkout(checkoutCommand)

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = checkoutResult.amount
        )

        paymentStatusUpdatePort.updatePaymentStatusToExecuting(
            paymentConfirmCommand.orderId,
            paymentConfirmCommand.paymentKey
        )

        val paymentStatusUpdateCommand = PaymentStatusUpdateCommand(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            status = PaymentStatus.UNKNOWN,
            failure = PaymentFailure("UNKNOWN", "UNKNOWN")
        )

        paymentStatusUpdatePort.updatePaymentStatus(paymentStatusUpdateCommand)

        return paymentConfirmCommand
    }

    private fun createPaymentExecutionResult(paymentConfirmCommand: PaymentConfirmCommand): PaymentExecutionResult {
        return PaymentExecutionResult(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.DONE,
                approvedAt = LocalDateTime.now().toKotlinLocalDateTime(),
                pspRawData = "{}"
            ),
            isSuccess = true,
            isFailure = false,
            isUnknown = false,
            isRetryable = false
        )
    }
}
