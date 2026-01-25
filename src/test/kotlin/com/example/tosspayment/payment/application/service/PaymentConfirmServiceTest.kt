package com.example.tosspayment.payment.application.service

import com.example.tosspayment.payment.adapter.out.persistence.PaymentMethod
import com.example.tosspayment.payment.adapter.out.persistence.PaymentStatus
import com.example.tosspayment.payment.adapter.out.persistence.PaymentType
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.application.port.out.PaymentExecutorPort
import com.example.tosspayment.payment.application.port.out.PaymentStatusUpdatePort
import com.example.tosspayment.payment.application.port.out.PaymentValidationPort
import com.example.tosspayment.payment.domain.PSPConfirmationStatus
import com.example.tosspayment.payment.domain.PaymentExecutionFailure
import com.example.tosspayment.payment.domain.PaymentExecutionResult
import com.example.tosspayment.payment.domain.PaymentExtraDetails
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toKotlinLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class PaymentConfirmServiceTest {

    private lateinit var paymentStatusUpdatePort: PaymentStatusUpdatePort
    private lateinit var paymentValidationPort: PaymentValidationPort
    private lateinit var paymentExecutorPort: PaymentExecutorPort
    private lateinit var paymentConfirmService: PaymentConfirmService

    @BeforeEach
    fun setUp() {
        paymentStatusUpdatePort = mockk()
        paymentValidationPort = mockk()
        paymentExecutorPort = mockk()

        paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = paymentExecutorPort
        )
    }

    @Test
    @DisplayName("결제 승인 성공 시 SUCCESS 상태를 반환한다")
    fun `should return SUCCESS when payment confirmation succeeds`() = runTest {
        // given
        val orderId = UUID.randomUUID().toString()
        val paymentKey = UUID.randomUUID().toString()
        val amount = 10000L

        val command = PaymentConfirmCommand(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = amount
        )

        val paymentExecutionResult = createSuccessPaymentExecutionResult(paymentKey, orderId, amount)

        coEvery { paymentStatusUpdatePort.updatePaymentStatusToExecuting(orderId, paymentKey) } returns true
        coEvery { paymentValidationPort.isValid(orderId, amount) } returns Unit
        coEvery { paymentExecutorPort.execute(command) } returns paymentExecutionResult
        coEvery { paymentStatusUpdatePort.updatePaymentStatus(any()) } returns true

        // when
        val result = paymentConfirmService.confirm(command)

        // then
        assertThat(result.status).isEqualTo(PaymentStatus.SUCCESS)

        coVerify(exactly = 1) { paymentStatusUpdatePort.updatePaymentStatusToExecuting(orderId, paymentKey) }
        coVerify(exactly = 1) { paymentValidationPort.isValid(orderId, amount) }
        coVerify(exactly = 1) { paymentExecutorPort.execute(command) }
        coVerify(exactly = 1) {
            paymentStatusUpdatePort.updatePaymentStatus(
                match { it.status == PaymentStatus.SUCCESS }
            )
        }
    }

    @Test
    @DisplayName("결제 승인 실패 시 FAILURE 상태를 반환한다")
    fun `should return FAILURE when payment confirmation fails`() = runTest {
        // given
        val orderId = UUID.randomUUID().toString()
        val paymentKey = UUID.randomUUID().toString()
        val amount = 10000L

        val command = PaymentConfirmCommand(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = amount
        )

        val paymentExecutionResult = createFailurePaymentExecutionResult(paymentKey, orderId)

        coEvery { paymentStatusUpdatePort.updatePaymentStatusToExecuting(orderId, paymentKey) } returns true
        coEvery { paymentValidationPort.isValid(orderId, amount) } returns Unit
        coEvery { paymentExecutorPort.execute(command) } returns paymentExecutionResult
        coEvery { paymentStatusUpdatePort.updatePaymentStatus(any()) } returns true

        // when
        val result = paymentConfirmService.confirm(command)

        // then
        assertThat(result.status).isEqualTo(PaymentStatus.FAILURE)

        coVerify(exactly = 1) {
            paymentStatusUpdatePort.updatePaymentStatus(
                match { it.status == PaymentStatus.FAILURE }
            )
        }
    }

    @Test
    @DisplayName("결제 승인 시 알 수 없는 오류 발생 시 UNKNOWN 상태를 반환한다")
    fun `should return UNKNOWN when payment confirmation has unknown error`() = runTest {
        // given
        val orderId = UUID.randomUUID().toString()
        val paymentKey = UUID.randomUUID().toString()
        val amount = 10000L

        val command = PaymentConfirmCommand(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = amount
        )

        val paymentExecutionResult = createUnknownPaymentExecutionResult(paymentKey, orderId)

        coEvery { paymentStatusUpdatePort.updatePaymentStatusToExecuting(orderId, paymentKey) } returns true
        coEvery { paymentValidationPort.isValid(orderId, amount) } returns Unit
        coEvery { paymentExecutorPort.execute(command) } returns paymentExecutionResult
        coEvery { paymentStatusUpdatePort.updatePaymentStatus(any()) } returns true

        // when
        val result = paymentConfirmService.confirm(command)

        // then
        assertThat(result.status).isEqualTo(PaymentStatus.UNKNOWN)

        coVerify(exactly = 1) {
            paymentStatusUpdatePort.updatePaymentStatus(
                match { it.status == PaymentStatus.UNKNOWN }
            )
        }
    }

    @Test
    @DisplayName("결제 승인 시 올바른 순서로 호출된다 (상태 업데이트 -> 검증 -> 실행 -> 상태 업데이트)")
    fun `should call ports in correct order`() = runTest {
        // given
        val orderId = UUID.randomUUID().toString()
        val paymentKey = UUID.randomUUID().toString()
        val amount = 10000L

        val command = PaymentConfirmCommand(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = amount
        )

        val paymentExecutionResult = createSuccessPaymentExecutionResult(paymentKey, orderId, amount)

        coEvery { paymentStatusUpdatePort.updatePaymentStatusToExecuting(orderId, paymentKey) } returns true
        coEvery { paymentValidationPort.isValid(orderId, amount) } returns Unit
        coEvery { paymentExecutorPort.execute(command) } returns paymentExecutionResult
        coEvery { paymentStatusUpdatePort.updatePaymentStatus(any()) } returns true

        // when
        paymentConfirmService.confirm(command)

        // then
        coVerify {
            paymentStatusUpdatePort.updatePaymentStatusToExecuting(orderId, paymentKey)
            paymentValidationPort.isValid(orderId, amount)
            paymentExecutorPort.execute(command)
            paymentStatusUpdatePort.updatePaymentStatus(any())
        }
    }

    @Test
    @DisplayName("결제 상태 업데이트 시 올바른 PaymentStatusUpdateCommand가 전달된다")
    fun `should pass correct PaymentStatusUpdateCommand when updating status`() = runTest {
        // given
        val orderId = UUID.randomUUID().toString()
        val paymentKey = UUID.randomUUID().toString()
        val amount = 10000L

        val command = PaymentConfirmCommand(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = amount
        )

        val paymentExecutionResult = createSuccessPaymentExecutionResult(paymentKey, orderId, amount)

        coEvery { paymentStatusUpdatePort.updatePaymentStatusToExecuting(orderId, paymentKey) } returns true
        coEvery { paymentValidationPort.isValid(orderId, amount) } returns Unit
        coEvery { paymentExecutorPort.execute(command) } returns paymentExecutionResult
        coEvery { paymentStatusUpdatePort.updatePaymentStatus(any()) } returns true

        // when
        paymentConfirmService.confirm(command)

        // then
        coVerify {
            paymentStatusUpdatePort.updatePaymentStatus(
                match<PaymentStatusUpdateCommand> {
                    it.paymentKey == paymentKey &&
                    it.orderId == orderId &&
                    it.status == PaymentStatus.SUCCESS &&
                    it.extraDetails != null
                }
            )
        }
    }

    private fun createSuccessPaymentExecutionResult(
        paymentKey: String,
        orderId: String,
        amount: Long
    ): PaymentExecutionResult {
        return PaymentExecutionResult(
            paymentKey = paymentKey,
            orderId = orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = amount,
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

    private fun createFailurePaymentExecutionResult(
        paymentKey: String,
        orderId: String
    ): PaymentExecutionResult {
        return PaymentExecutionResult(
            paymentKey = paymentKey,
            orderId = orderId,
            extraDetails = null,
            failure = PaymentExecutionFailure(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                approvedAt = LocalDateTime.now().toKotlinLocalDateTime(),
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.ABORTED,
                totalAmount = 10000L,
                pspRawData = "{}"
            ),
            isSuccess = false,
            isFailure = true,
            isUnknown = false,
            isRetryable = false
        )
    }

    private fun createUnknownPaymentExecutionResult(
        paymentKey: String,
        orderId: String
    ): PaymentExecutionResult {
        return PaymentExecutionResult(
            paymentKey = paymentKey,
            orderId = orderId,
            extraDetails = null,
            failure = null,
            isSuccess = false,
            isFailure = false,
            isUnknown = true,
            isRetryable = true
        )
    }
}
