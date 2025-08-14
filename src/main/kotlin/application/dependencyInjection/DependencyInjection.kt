package caio.caminha.application.dependencyInjection

import caio.caminha.application.client.InternalClient
import caio.caminha.application.client.PaymentProcessorClient
import caio.caminha.application.client.clientConfiguration
import caio.caminha.application.repository.InMemoryRepository
import caio.caminha.application.worker.PoolWorkerService
import caio.caminha.utils.KotlinSerializationJsonParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

//Dependency Management
/**
 * Not sure how it works, if I can just Inject this GreetingService anywhere
 */
fun Application.configureDependencyInjection() {
    dependencies {
        provide<InMemoryRepository> {
            InMemoryRepository()
        }

        provide<PoolWorkerService> { PoolWorkerService() }

        provide<HttpClient> {
            clientConfiguration()
        }
    }
}

fun Application.internalClientDependencyInjection(
    internalApi: String,
    httpClient: HttpClient,
) {
    dependencies {
        provide<InternalClient> {
            InternalClient(
                httpClient = httpClient,
                internalApi = internalApi,
            )
        }
    }
}


fun Application.paymentProcessorClientDependency(
    defaultPaymentProcessorApi: String,
    fallBackPaymentProcessorApi: String,
) {
    // todo check if this works - not very confident about it
    val httpClient: HttpClient by dependencies
    dependencies {
        provide<PaymentProcessorClient> {
            PaymentProcessorClient(
                defaultPaymentProcessorApi = defaultPaymentProcessorApi,
                fallBackPaymentProcessorApi = fallBackPaymentProcessorApi,
                httpClient = httpClient,
            )
        }
    }
}