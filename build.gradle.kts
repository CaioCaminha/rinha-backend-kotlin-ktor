plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "caio.caminha"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

graalvmNative {
    binaries {
        named("main") {
            // Add these build arguments
            buildArgs.addAll(listOf(
                "-H:+ReportExceptionStackTraces",
                "-H:+AddAllCharsets",  // Critical for JDK internals
                "-H:+TraceClassInitialization",
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

dependencies {
    implementation(libs.ktor.server.di)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
