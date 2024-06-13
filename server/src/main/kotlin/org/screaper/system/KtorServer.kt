package org.screaper.system

import SERVER_PORT
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import org.screaper.db.ScreaperLogDataRepository
import screaper.Screaper
import screaper.ScreaperRequest
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


fun ktorServer(
    screaper: Screaper,
    screaperLogDataRepository: ScreaperLogDataRepository,
) = embeddedServer(
    Netty,
    port = SERVER_PORT,
    host = "0.0.0.0",
//        configure = {
//            val myParallelism = 32 # caution!
//            connectionGroupSize = myParallelism / 2 + 1
//            workerGroupSize = myParallelism / 2 + 1
//            callGroupSize = myParallelism
//        },
) {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        route("/screaper") {
            post("/calculate/{multiplier}") {
                call.respond(
                    screaper.screap(
                        _request = call.receive<ScreaperRequest>(),
                        multiplier = call.parameters["multiplier"]?.toIntOrNull(),
                    )
                )
            }
            route("/log") {
                get {
                    call.respond(screaperLogDataRepository.findAll().map { it.result })
                }
                delete {
                    call.respond(screaperLogDataRepository.deleteAll())
                }
            }
        }


        get("/emulator/{id}") {
            if ((0..10).random() == 0) {
                delay(10.seconds)
            } else {
                delay((0..1000).random().milliseconds)
            }
            if ((0..5).random() == 0) {
                1 / 0
            }
            val id = call.parameters["id"]
            val price = id?.toIntOrNull() ?: 100
            call.respond(
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Product (${id})</title>
                </head>
                <body>
                    <h1 asd-asd="dsadas">
                        Product Name: Awesome Stuff
                    </h1>
                    <p>Price: ${'$'}$price.99</p>
                </body>
                </html>
            """.trimIndent()
            )
        }
    }
}