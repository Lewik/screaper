plugins {
    alias(libs.plugins.kotlinJvm)
//    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    application
    alias(libs.plugins.bmuschko.docker.java.application)
}

group = "org.screaper"
version = "1.0.0"
application {
    mainClass.set("org.screaper.system.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

docker {
    javaApplication {
        maintainer.set("screaper")
        ports.set(emptyList())
        baseImage.set("azul/zulu-openjdk:22")

        images.set(
            setOf(
                "screaper/$name:latest",
            )
        )
    }
}

dependencies {
    implementation(libs.logback)

    implementation(projects.shared)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.kotlinx.json)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)


    implementation(libs.date.time)
    implementation(libs.mongo)
    implementation(libs.bson)


    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}
