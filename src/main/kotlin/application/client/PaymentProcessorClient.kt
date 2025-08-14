package caio.caminha.application.client

import caio.caminha.domain.PaymentDetails
import caio.caminha.utils.toJsonString
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PaymentProcessorClient(
    val httpClient: HttpClient,
    private val defaultPaymentProcessorApi: String,
    private val fallBackPaymentProcessorApi: String,
) {

    suspend fun sendPayment(
        paymentDetails: PaymentDetails,
    ): PaymentDetails? = coroutineScope {
        if(
            callPaymentProcessor(
                url = defaultPaymentProcessorApi,
                paymentDetails = paymentDetails,
            ).await()
        ) {
            println("Successfully called default processor")
            paymentDetails
        } else {
            callPaymentProcessor(
                url = fallBackPaymentProcessorApi,
                paymentDetails = paymentDetails,
            ).await().let { bool ->
                if (bool)
                    paymentDetails.also {
                        println("Successfully called fallback processor")
                    }
                else
                    null
            }
        }
    }




    private fun CoroutineScope.callPaymentProcessor(
        url: String,
        paymentDetails: PaymentDetails,
    ): Deferred<Boolean> = async {
        httpClient.post("$url/payments") {
            contentType(ContentType.Application.Json)
            setBody(paymentDetails.toJsonString())
        }.status.isSuccess()
    }

}