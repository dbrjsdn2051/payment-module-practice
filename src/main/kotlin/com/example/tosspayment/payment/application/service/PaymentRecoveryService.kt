package com.example.tosspayment.payment.application.service

import com.example.tosspayment.common.UseCase
import com.example.tosspayment.payment.application.port.`in`.PaymentConfirmCommand
import com.example.tosspayment.payment.application.port.`in`.PaymentRecoveryUseCase
import com.example.tosspayment.payment.application.port.`in`.PaymentStatusUpdateCommand
import com.example.tosspayment.payment.application.port.out.LoadPendingPaymentPort
import com.example.tosspayment.payment.application.port.out.PaymentExecutorPort
import com.example.tosspayment.payment.application.port.out.PaymentStatusUpdatePort
import com.example.tosspayment.payment.application.port.out.PaymentValidationPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@UseCase
class PaymentRecoveryService(
    private val loadPendingPaymentPort: LoadPendingPaymentPort,
    private val paymentValidationPort: PaymentValidationPort,
    private val paymentExecutorPort: PaymentExecutorPort,
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    private val paymentErrorHandler: PaymentErrorHandler
) : PaymentRecoveryUseCase {

    private val dispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(r, "recovery").apply { isDaemon = true }
    }.asCoroutineDispatcher()

    private val scope = CoroutineScope(dispatcher)

    @Scheduled(fixedDelay = 180, initialDelay = 180, timeUnit = TimeUnit.SECONDS)
    override suspend fun recovery() {
        val pendingPayments = loadPendingPaymentPort.getPendingPayments()

        pendingPayments.forEach { paymentEvent ->
            scope.launch {
                val command = PaymentConfirmCommand(
                    paymentKey = paymentEvent.paymentKey,
                    orderId = paymentEvent.orderId,
                    amount = paymentEvent.totalAmount()
                )

                try {
                    paymentValidationPort.isValid(command.orderId, command.amount)
                    val result = paymentExecutorPort.execute(command)
                    paymentStatusUpdatePort.updatePaymentStatus(PaymentStatusUpdateCommand(result))
                } catch (e: Exception) {
                    paymentErrorHandler.handlePaymentConfirmationError(e, command)
                }
            }
        }
    }
}