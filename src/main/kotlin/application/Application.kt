package caio.caminha.application

import caio.caminha.application.dependencyInjection.configureDependencyInjection
import caio.caminha.application.dependencyInjection.internalClientDependencyInjection
import caio.caminha.application.dependencyInjection.paymentProcessorClientDependency
import caio.caminha.application.routing.configureRouting
import caio.caminha.application.worker.PoolWorkerService.Companion.startWorkerPool
import caio.caminha.utils.configureSerialization
import io.ktor.client.HttpClient
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    /**
     * Engine CIO (Coroutine I/O) is required for native / graalvm
     */
    io.ktor.server.cio.EngineMain.main(args)
}

suspend fun Application.module() {

    configureSerialization()
    configureDependencyInjection()

    paymentProcessorClientDependency(
        defaultPaymentProcessorApi = environment.config.property("client.payment-processor.default.url").getString(),
        fallBackPaymentProcessorApi = environment.config.property("client.payment-processor.fallback.url").getString(),
    )

    internalClientDependencyInjection(
        internalApi = environment.config.property("client.internal.url").getString(),
        httpClient = dependencies.resolve<HttpClient>(),
    )

    configureRouting()

    /**
     * Launches workers when application starts
     */
    monitor.subscribe(ApplicationStarted) {
        launch {
            startWorkerPool()
        }
    }

}
