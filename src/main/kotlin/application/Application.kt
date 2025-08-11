package caio.caminha.application

import caio.caminha.configureFrameworks
import caio.caminha.configureRouting
import caio.caminha.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureSerialization()
    configureRouting()
}
