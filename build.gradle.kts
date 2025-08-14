plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "caio.caminha"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.cio.EngineMain"
}

graalvmNative {
    binaries {
        named("main") {
            // Add these build arguments
            buildArgs.addAll(listOf(
                "-H:+ReportExceptionStackTraces",
                "-H:+AddAllCharsets",  // Critical for JDK internals
                "-H:+ReportExceptionStackTraces",
                "--report-unsupported-elements-at-runtime",
                "--initialize-at-build-time=jdk.internal.misc.Unsafe",
                "--initialize-at-run-time=jdk.internal.misc",  // Initialize at runtime
                "--initialize-at-build-time=com.oracle.svm.core.graal.jdk.SubstrateObjectCloneSnippets",  // Initialize at runtime
                "-H:ClassInitialization=:build_time",  // Default init policy
                "--trace-class-initialization=jdk.internal.misc.ScopedMemoryAccess"
            ))
        }
    }
}

ktor {
    development = true
}

dependencies {
    implementation(libs.ktor.server.di)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
//    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation("io.ktor:ktor-client-logging:3.2.3")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation("io.ktor:ktor-server-cio:3.2.3")
//    implementation("io.insert-koin:koin-logger-slf4j:4.0.0")
}
