package org.screaper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import screaper.CoroutineScreaper
import screaper.ScreaperRequest

fun main(args: Array<String>) = TheCommand().main(args)

class TheCommand : CliktCommand() {
    val urls by option("--url", help = "Urls (separate by comma, use (i) for multiplier)")
        .split(",")
        .default(listOf("http://0.0.0.0:8080/emulator/(i)"))
    val multiplier by option("-m", "--multiplier", help = "Multiplier")
        .int()
        .default(10)
    val regexps: Map<String, String> by option(
        "-r",
        "--regexps",
        help = "Regexps (define regexp in each row, format is label=regexp)"
    )
        .associate()


    override fun run() {
        runBlocking {
            val regexps1 = regexps ?: mapOf(
                "price" to Regex("<h1>.*?:\\s*(.*?)</h1>").toString(),
                "label" to Regex("<p>.*?:\\s*(.*?)</p>").toString(),
            )

            val screaperHttpClient = HttpClient {
                expectSuccess = true
                install(HttpTimeout) {
                    requestTimeoutMillis = 5000
                }
            }
            val result = CoroutineScreaper(
                screaperHttpClient
            )
                .screap(
                    ScreaperRequest(
                        urls = urls,
                        regexPatterns = regexps1,
                    )
                )
            val json = Json { prettyPrint = true }
            echo(json.encodeToString(result))

        }
    }
}


//fun Application.module() {
//
//    install(CORS) {
//        anyHost()
//        allowHeader(HttpHeaders.ContentType)
//    }
//    install(ContentNegotiation) {
//        json()
//    }
//    routing {
//        get("/") {
//            call.respondText("Ktor: HI")
//        }
//        post("/screaper/calculate/{multiplier}") {
//            var request = call.receive<ScreaperRequest>()
//            val multiplier = call.parameters["multiplier"]?.toIntOrNull() ?: 1
//
//            if (multiplier > 0 && request.urls.size == 1) {
//                val url = request.urls.single()
//                request = request.copy(
//                    urls = (1..multiplier).map {
//                        url.replace("(i)", it.toString())
//                    }
//                )
//            }
//
//            val screaperHttpClient = HttpClient {
//                expectSuccess = true
//                install(HttpTimeout) {
//                    requestTimeoutMillis = 5000
//                }
//            }
//            val result = CoroutineScreaper(
//                screaperHttpClient
//            )
//                .screap(request)
//
//            call.respond(result)
//
//        }
//
//
//        get("/emulator/{id}") {
//            if ((0..10).random() == 0) {
//                delay(10.seconds)
//            } else {
//                delay((0..1000).random().milliseconds)
//            }
//            if ((0..5).random() == 0) {
//                1 / 0
//            }
//            val id = call.parameters["id"]
//            val price = id?.toIntOrNull() ?: 100
//            call.respond(
//                """
//                <!DOCTYPE html>
//                <html lang="en">
//                <head>
//                    <meta charset="UTF-8">
//                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
//                    <title>Product (${id})</title>
//                </head>
//                <body>
//                    <h1>Product Name: Awesome Stuff</h1>
//                    <p>Price: ${'$'}$price.99</p>
//                </body>
//                </html>
//            """.trimIndent()
//            )
//        }
//    }
//
//}
