package caio.caminha.application.client

import caio.caminha.domain.PaymentSummaryResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import java.time.Instant

class InternalClient(
    val httpClient: HttpClient,
    private val internalApi: String,
) {
    suspend fun getPaymentsSummary(
        from: Instant?,
        to: Instant?,
    ): PaymentSummaryResponse {
        return httpClient.get("$internalApi/payments-summary") {
            headers {
                append("isInternalRequest", true.toString())
                append(HttpHeaders.Accept, ContentType.Application.Json.contentType)
            }
            url {
                if(from != null && to != null) {
                    parameters.append("from", from.toString())
                    parameters.append("to", to.toString())
                }
            }
        }.body<PaymentSummaryResponse>()
    }

    suspend fun purgePayments() {
        httpClient.post("$internalApi/purge-payments?internalRequest=true") {
            contentType(ContentType.Application.Json)
        }
    }
}