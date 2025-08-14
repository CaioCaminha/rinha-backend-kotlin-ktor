package caio.caminha.domain

import kotlinx.serialization.Serializable

@Serializable
data class PaymentDto (
    val correlationId: String,
    val amount: String
)