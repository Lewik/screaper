package screaper

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlin.time.measureTimedValue

/**
 * Implementation of screaper based on kotlin coroutines
 */
class CoroutineScreaper(
    val client: HttpClient,
) : Screaper {
    //suspend is kotlin version of async/await
    override suspend fun screap(request: ScreaperRequest): ScreaperResult {
        val urls = request.urls
        val regexPatterns = request.regexPatterns
        val taskStartTime = Clock.System.now()
        // coroutineScope will allow to run multiple coroutines inside and await them.
        val (entries, overallDuration) = coroutineScope {
            measureTimedValue {
                /**
                 * For each url we create (async) coroutine for request
                 * Coroutines will start and creation moment
                 * Coroutines will be executed in parallel on all available threads
                 * Available thread controlled by ktor client library
                 * Ktor client library for jmv will use predefined IO Dispatcher
                 * Default limit is 64 threads or the number of cores (whichever is larger).
                 * If we run this code on js - it will use single thread but still in async way.
                 * I think, it's possible to run WebWorkers for this task, but it will consume more code due WebWorkers limitations.
                 * Bur for wasm there should not be any restrictions and code should work as is.
                 *
                 *
                 */

                urls
                    .map { url ->
                        async {
                            val startTime = Clock.System.now()

                            val (data, duration) = measureTimedValue {
                                try {
                                    // request execution
                                    val html = client.get(url).bodyAsText()
                                    html.extract(regexPatterns) to null
                                } catch (error: Throwable) {
                                    if (error is ResponseException) {
                                        emptyMap<String, String>() to error.response.toString()
                                    } else {
                                        emptyMap<String, String>() to error.message
                                    }
                                }
                            }

                            val (results, error) = data

                            ScreaperResult.Entry(
                                url = url,
                                results = results,
                                duration = duration,
                                startTime = startTime,
                                error = error,
                            )
                        }
                    }
                    .awaitAll()
            }
        }

        return ScreaperResult(
            entries = entries,
            overallDuration = overallDuration,
            overallStartTime = taskStartTime,
        )
    }

    /**
     * kotlin feature Extension functions (syntax sugar): we can declare function for particular type and for particular scope
     */
    private fun String.extract(
        regexPatterns: Map<String, String>,
    ) = regexPatterns
        .map { (name, regex) ->
            // find first result of regexp
            name to (regex.toRegex().find(this)?.groupValues?.getOrNull(1) ?: "")
        }
        .toMap()
}