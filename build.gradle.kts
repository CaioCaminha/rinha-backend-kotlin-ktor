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
            buildArgs.add("--initialize-at-build-time=jdk.internal.misc.VM")
            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("--initialize-at-build-time=io.ktor,kotlinx,kotlin")
            buildArgs.add("--initialize-at-build-time=ch.qos.logback.core.status.InfoStatus")
            buildArgs.add("--initialize-at-build-time=org.slf4j.helpers.Reporter")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")
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
    implementation(libs.ktor.server.config.yaml)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation("io.ktor:ktor-client-logging:3.2.3")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation("io.ktor:ktor-server-cio:3.2.3")
}
