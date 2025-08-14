package caio.caminha.application.routing

import caio.caminha.application.client.InternalClient
import caio.caminha.application.repository.InMemoryRepository
import caio.caminha.application.worker.PoolWorkerService
import caio.caminha.domain.PaymentDto
import caio.caminha.utils.KotlinSerializationJsonParser
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.server.application.*
import io.ktor.server.plugins.di.annotations.Named
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

fun Application.configureRouting() {
    val poolWorkerService: PoolWorkerService by dependencies
    val repository: InMemoryRepository by dependencies
    val internalClient: InternalClient by dependencies
    routing {
        createBasicController(
            poolWorkerService = poolWorkerService,
            internalClient = internalClient,
            repository = repository,
        )
    }
}

/**
 * It's a functional web framework
 * Each route it's defining a handler
 */
fun Route.createBasicController(
    poolWorkerService: PoolWorkerService,
    repository: InMemoryRepository,
    internalClient: InternalClient,
) {
    route("/payments") {
        post {
            println("received payment")
            val body = call.receiveText()
            println("body received: $body")
            poolWorkerService.enqueue(
                paymentDto = KotlinSerializationJsonParser.DEFAULT_KOTLIN_SERIALIZATION_PARSER.decodeFromString(body),
            )
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/payments-summary") {
        get {
            val isInternalRequest = call.request.headers["IsInternalCall"]
            val from = call.request.queryParameters["from"]?.let { Instant.parse(it) }
            val to = call.request.queryParameters["to"]?.let { Instant.parse(it) }

            val response = repository
                .getSummary(from = from, to = to ) { from, to ->
                    if(isInternalRequest == null) {
                        internalClient.getPaymentsSummary(from = from, to = to)
                    } else {
                        null
                    }
                }

            call.respond(HttpStatusCode.OK, response)
        }
    }

    route("/purge-payments") {
        post {
            val isInternalRequest = call.request.headers["IsInternalCall"].isNullOrEmpty()
            repository.purge(
                isInternalRequest = isInternalRequest,
            ) { internalClient.purgePayments() }
        }
    }
}
