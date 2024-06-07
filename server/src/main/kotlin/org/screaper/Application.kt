package org.screaper

import SERVER_PORT
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.client.*
import io.ktor.client.plugins.*
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
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import screaper.CoroutineScreaper
import screaper.ScreaperRequest
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


//val connectionString = "mongodb://localhost:27017/screaper"
//val mongoClient = MongoClient.create(connectionString = connectionString)
//val databaseName = "screaper"
//val mongoDb = mongoClient.getDatabase(databaseName = databaseName)
//val screaperLogMongoCollection = mongoDb.getCollection<ScreaperLog>("screaperlog")

val screaperLogMongoCollection: MongoCollection<ScreaperLog>? = null

fun main() {
    embeddedServer(
        Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
//        configure = {
//            val myParallelism = 32 # caution!
//            connectionGroupSize = myParallelism / 2 + 1
//            workerGroupSize = myParallelism / 2 + 1
//            callGroupSize = myParallelism
//        },
        module = Application::module,
    )
        .start(wait = true)
}


fun Application.module() {

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/") {
            call.respondText("Ktor: HI")
        }
        post("/screaper/calculate/{multiplier}") {
            var request = call.receive<ScreaperRequest>()
            val multiplier = call.parameters["multiplier"]?.toIntOrNull() ?: 1

            request = request.copyWithMultipliedUrls(multiplier)

            val screaperHttpClient = HttpClient {
                expectSuccess = true
                install(HttpTimeout) {
                    requestTimeoutMillis = 5000
                }
            }
            val result = CoroutineScreaper(
                screaperHttpClient
            )
                .screap(request)


            try {
                if (screaperLogMongoCollection != null) {
                    screaperLogMongoCollection.insertOne(
                        ScreaperLog(
                            id = ObjectId(),
                            result = result,
                        )
                    )
                }
            } catch (throwable: Throwable) {
                println(throwable)
            }

            call.respond(result)

        }

        get("/screaper/log") {
            if (screaperLogMongoCollection != null) {
                call.respond(screaperLogMongoCollection.find().toList().map { it.result })
            }
        }

        delete("/screaper/log") {
            if (screaperLogMongoCollection != null) {
                call.respond(screaperLogMongoCollection.drop())
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
                    <h1>Product Name: Awesome Stuff</h1>
                    <p>Price: ${'$'}$price.99</p>
                </body>
                </html>
            """.trimIndent()
            )
        }
    }

}

