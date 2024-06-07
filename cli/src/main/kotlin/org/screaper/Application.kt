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

fun main(args: Array<String>) = ScrapeCommand().main(args)

class ScrapeCommand : CliktCommand() {
    val urls by option("--url", help = "Urls (separate by comma, use (i) for multiplier)")
        .split(",")
        .default(listOf("http://0.0.0.0:8080/emulator/(i)"))
    val multiplier by option("-m", "--multiplier", help = "Multiplier")
        .int()
        .default(10)
    val regexpsValue: Map<String, String> by option(
        "-r",
        "--regexps",
        help = "Regexps (define regexp in each row, format is label=regexp)"
    )
        .associate()


    override fun run() {
        runBlocking {
            val regexps = if (regexpsValue.isEmpty()) {
                mapOf(
                    "price" to Regex("<h1>.*?:\\s*(.*?)</h1>").toString(),
                    "label" to Regex("<p>.*?:\\s*(.*?)</p>").toString(),
                )
            } else {
                regexpsValue
            }

            val screaperHttpClient = HttpClient {
                expectSuccess = true
                install(HttpTimeout) {
                    requestTimeoutMillis = 5000
                }
            }
            val result = CoroutineScreaper(screaperHttpClient)
                .screap(
                    ScreaperRequest(
                        urls = urls,
                        regexPatterns = regexps,
                    )
                        .copyWithMultipliedUrls(multiplier)
                )
            val json = Json { prettyPrint = true }
            echo(json.encodeToString(result))

        }
    }
}
