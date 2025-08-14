package caio.caminha.application.client

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

fun clientConfiguration(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(KotlinSerializationJsonParser.DEFAULT_KOTLIN_SERIALIZATION_PARSER)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { request, response ->
                !response.status.isSuccess()
            }
            delayMillis { retry ->
                retry * 1500L
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 1000
        }
        engine { requestTimeout = 0}
    }
}