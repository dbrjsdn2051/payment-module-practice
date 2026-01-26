package com.example.tosspayment.payment.application.service

import com.example.tosspayment.common.Logger
import com.example.tosspayment.common.UseCase
import com.example.tosspayment.payment.application.port.`in`.PaymentEventMessageRelayUseCase
import com.example.tosspayment.payment.application.port.out.DispatchEventMessagePort
import com.example.tosspayment.payment.application.port.out.LoadPendingPaymentEventMessagePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@UseCase
@Profile("dev")
class PaymentEventMessageRelayService(
    private val loadPendingPaymentEventMessagePort: LoadPendingPaymentEventMessagePort,
    private val dispatchEventMessagePort: DispatchEventMessagePort
) : PaymentEventMessageRelayUseCase {

    private val dispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(r, "message-relay").apply { isDaemon = true }
    }.asCoroutineDispatcher()

    private val scope = CoroutineScope(dispatcher)

    @Scheduled(fixedDelay = 180, initialDelay = 180, timeUnit = TimeUnit.SECONDS)
    override suspend fun relay() {
        scope.launch {
            try {
                val pendingMessages = loadPendingPaymentEventMessagePort.getPendingPaymentEventMessage()
                pendingMessages.forEach { message ->
                    try {
                        dispatchEventMessagePort.dispatch(message)
                    } catch (e: Exception) {
                        Logger.error("messageRelay", e.message ?: "failed to relay message.", e)
                    }
                }
            } catch (e: Exception) {
                Logger.error("messageRelay", e.message ?: "failed to load pending messages.", e)
            }
        }
    }
}
