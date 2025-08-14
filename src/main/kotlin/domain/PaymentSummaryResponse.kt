package caio.caminha.domain

import caio.caminha.utils.serializer.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
class PaymentSummaryResponse (
    val default: PaymentSummary,
    val fallback: PaymentSummary
)

@Serializable
data class PaymentSummary(
    val totalRequests: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val totalAmount: BigDecimal,
)