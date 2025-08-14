package caio.caminha.domain

import caio.caminha.utils.serializer.BigDecimalSerializer
import caio.caminha.utils.serializer.InstantSerializer
import caio.caminha.utils.serializer.UUIDSerializer
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDetails (
    @Serializable(with = UUIDSerializer::class)
    val correlationId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    @Serializable(with = InstantSerializer::class)
    val requestedAt: Instant = Instant.now(),
    var paymentProcessorType: PaymentProcessorType,
)


enum class PaymentProcessorType {
    DEFAULT, FALLBACK;
}