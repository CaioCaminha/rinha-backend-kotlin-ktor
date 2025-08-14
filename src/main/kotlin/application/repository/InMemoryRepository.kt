package caio.caminha.application.repository

import caio.caminha.domain.PaymentDetails
import caio.caminha.domain.PaymentProcessorType
import caio.caminha.domain.PaymentSummary
import caio.caminha.domain.PaymentSummaryResponse
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.plus

class InMemoryRepository {

    private val payments = LinkedBlockingQueue<PaymentDetails>()
    private val defaultPaymentSummary = PaymentSummaryResults()
    private val fallbackPaymentSummary = PaymentSummaryResults()


    fun add(
        paymentDetails: PaymentDetails,
    ) {
        println("Adding payment $paymentDetails to InMemoryDatabase")
        if(payments.add(paymentDetails)) {
            when(paymentDetails.paymentProcessorType) {
                PaymentProcessorType.DEFAULT -> defaultPaymentSummary.incrementValues(
                    amount = paymentDetails.amount,
                ).also {
                    println("Payment added to default summary results")
                }
                PaymentProcessorType.FALLBACK -> fallbackPaymentSummary.incrementValues(
                    amount = paymentDetails.amount,
                ).also {
                    println("Payment added to fallback summary results")
                }
            }
        } else {
            println("Failed to save payment: $paymentDetails")
        }
    }


    suspend fun getSummary(
        from: Instant? = null,
        to: Instant? = null,
        syncBlock: suspend (Instant?, Instant?) -> PaymentSummaryResponse?,
    ): PaymentSummaryResponse {
        if(from != null && to != null) {
            /**
             * Given the fact that these properties are inside this method scope, is it
             * really necessary to wrap with an AtomicReference?
             */
            val defaultAmount = AtomicReference<BigDecimal>(BigDecimal.ZERO)
            val fallBackAmount = AtomicReference<BigDecimal>(BigDecimal.ZERO)

            val paymentsByType = payments.filter { details ->
                details.requestedAt.isAfter(from) &&
                        details.requestedAt.isBefore(to)
            }.groupBy { it.paymentProcessorType }

            println("paymentsByType: $paymentsByType")

            /**
             * Investigate a way of avoiding this traversing on two lists
             */
            paymentsByType[PaymentProcessorType.DEFAULT]
                ?.forEach { t -> defaultAmount.set(defaultAmount.get().plus(t.amount)) }

            paymentsByType[PaymentProcessorType.FALLBACK]
                ?.forEach { t -> fallBackAmount.set(fallBackAmount.get().plus(t.amount)) }

            return PaymentSummaryResponse(
                default = PaymentSummary(
                    totalAmount = defaultAmount.get(),
                    totalRequests = paymentsByType[PaymentProcessorType.DEFAULT]?.count() ?: 0,
                ),
                fallback = PaymentSummary(
                    totalAmount = fallBackAmount.get(),
                    totalRequests = paymentsByType[PaymentProcessorType.FALLBACK]?.count() ?: 0,
                )
            ).mergeResults(syncBlock(from, to))
        } else {
            println("From and To not informed, returning all values")
            return PaymentSummaryResponse(
                default = defaultPaymentSummary.toPaymentSummary(),
                fallback = fallbackPaymentSummary.toPaymentSummary()
            ).mergeResults(syncBlock(null, null))
        }
    }

    suspend fun purge(
        isInternalRequest: Boolean,
        syncBlock: suspend () -> Unit,
    ) {
        println("Cleaning List of Payments")
        payments.clear()

        println("Cleaning defaultPayments results")
        defaultPaymentSummary.totalRequests.set(0)
        defaultPaymentSummary.totalAmountAtomicReference.set(BigDecimal.ZERO)

        println("Cleaning fallbackPayments results")
        fallbackPaymentSummary.totalRequests.set(0)
        fallbackPaymentSummary.totalAmountAtomicReference.set(BigDecimal.ZERO)

        if(!isInternalRequest) {
            println("Calling purge from the other instance")
            syncBlock()
        } else {
            println("Finished Purging payments")
        }
    }

}

private fun PaymentSummaryResponse.mergeResults(
    syncPaymentSummaryResponse: PaymentSummaryResponse?
): PaymentSummaryResponse {
    println("merging values")
    return syncPaymentSummaryResponse?.let {
        PaymentSummaryResponse(
            default = PaymentSummary(
                totalRequests = this.default.totalRequests.plus(
                    syncPaymentSummaryResponse.default.totalRequests
                ),
                totalAmount = this.default.totalAmount.plus(
                    syncPaymentSummaryResponse.default.totalAmount
                )
            ),
            fallback = PaymentSummary(
                totalRequests = this.fallback.totalRequests.plus(
                    syncPaymentSummaryResponse.fallback.totalRequests
                ),
                totalAmount = this.fallback.totalAmount.plus(
                    syncPaymentSummaryResponse.fallback.totalAmount
                )
            )
        )
    } ?: this

}

data class PaymentSummaryResults(
    val totalRequests: AtomicInteger,
    val totalAmountAtomicReference: AtomicReference<BigDecimal>,
){
    constructor() : this(
        AtomicInteger(0),
        AtomicReference(BigDecimal.ZERO),
    )

    fun incrementValues(
        amount: BigDecimal,
    ) {
        totalRequests.incrementAndGet()
        totalAmountAtomicReference.set(totalAmountAtomicReference.get().plus(amount))
    }

    fun toPaymentSummary() = PaymentSummary(
        totalRequests = this.totalRequests.get(),
        totalAmount = this.totalAmountAtomicReference.get(),
    )
}