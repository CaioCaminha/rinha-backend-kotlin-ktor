package caio.caminha.application.worker

import caio.caminha.application.client.PaymentProcessorClient
import caio.caminha.application.repository.InMemoryRepository
import caio.caminha.domain.PaymentDetails
import caio.caminha.domain.PaymentDto
import caio.caminha.domain.PaymentProcessorType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.plugins.di.dependencies
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PoolWorkerService {

    companion object {

        private val workerPool = Channel<PaymentDto>(
            capacity = Channel.UNLIMITED,
            /**
             * Passing BufferOverflow.SUSPEND forces the producers to slow down addition of new payments
             * This is recommended to scenarios where "all elements must eventually be processed"
             *
             * Investigate if changing this wouldn't be better
             */
            onBufferOverflow = BufferOverflow.SUSPEND
        )

        /**
         * I'm not sure if getting the available processors it's a good Idea for coroutines
         *
         * Runtime.getRuntime().availableProcessors()
         */
        private val workerCount = 10

        suspend fun Application.startWorkerPool() = coroutineScope {
            val repository: InMemoryRepository by dependencies
            val paymentProcessorClient: PaymentProcessorClient by dependencies

            repeat(workerCount) { id ->
                launchWorker(
                    id = id,
                    paymentProcessorClient = paymentProcessorClient,
                    repository = repository,
                )
            }
        }


        private fun CoroutineScope.launchWorker(
            id: Int,
            paymentProcessorClient: PaymentProcessorClient,
            repository: InMemoryRepository,
        ) = launch {
            println("Started worker $id")
            workerPool.consumeEach { payment ->
                println("Consuming payment $payment")
                try {
                    println("trying to send payment to payment processor $payment")
                    withContext(NonCancellable) {
                        paymentProcessorClient.sendPayment(
                            paymentDetails = payment.toPaymentDetails()
                        )?.let { details ->
                            println("Successfully sent payment")
                            repository.add(
                                paymentDetails = details
                            )
                        }
                    }
                } catch (exception: Exception) {
                    println("Failed to process payment: ${exception.message}")
                }
            }
        }

    }

    fun enqueue(paymentDto: PaymentDto) {
        println("trying to save on queue $paymentDto")
        workerPool.trySend(paymentDto)
            .onSuccess {
                println("Successfully added payment to pool")
            }
            .onFailure {
                println("Failed to add payment to pool ${it?.message}")
            }
    }

}

fun PaymentDto.toPaymentDetails() = PaymentDetails(
    correlationId = UUID.fromString(this.correlationId),
    amount = BigDecimal(this.amount),
    requestedAt = Instant.now(),
    paymentProcessorType = PaymentProcessorType.DEFAULT,
)