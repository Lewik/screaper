plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

group = "org.screaper"
version = "1.0.0"
application {
    mainClass.set("org.screaper.ApplicationKt")
}

dependencies {
    implementation(libs.logback)

    implementation(projects.shared)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)

    implementation(libs.clikt)

}
