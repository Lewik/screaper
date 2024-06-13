package screaper

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import screaper.db.DataRepository
import screaper.entities.ScreaperLog
import screaper.utils.generateUuid
import kotlin.reflect.KClass
import kotlin.time.measureTimedValue

/**
 * Implementation of screaper based on kotlin coroutines
 */
class CoroutineScreaper(
    private val client: HttpClient,
    private val extractorMap: ExtractorMap,
    private val screaperLogDataRepository: DataRepository<ScreaperLog>,
) : Screaper {

    //suspend is kotlin version of async/await
    override suspend fun screap(_request: ScreaperRequest, multiplier: Int?): ScreaperResult {
        val request = if (multiplier != null) {
            _request.copyWithMultipliedUrls(multiplier)
        } else {
            _request
        }
        val urls = request.urls
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
                 * But for wasm there should not be any restrictions and code should work as is.
                 */

                urls
                    .map { url ->
                        async {
                            val startTime = Clock.System.now()

                            val (data, duration) = measureTimedValue {
                                try {
                                    // request execution
                                    val html = client.get(url).bodyAsText()
                                    request
                                        .tasks
                                        .mapValues { extractorMap.getFor(it.value).extract(it.value, html) } to null
                                } catch (error: Throwable) {
                                    if (error is ResponseException) {
                                        emptyMap<String, List<String>>() to error.response.toString()
                                    } else {
                                        emptyMap<String, List<String>>() to error.message
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

        val result = ScreaperResult(
            entries = entries,
            overallDuration = overallDuration,
            overallStartTime = taskStartTime,
        )


        screaperLogDataRepository.upsert(
            ScreaperLog(
                id = generateUuid().toString(),
                result = result,
            )
        )

        return result
    }


    class ExtractorMap {
        private val extractors: MutableMap<KClass<*>, Screaper.Extractor<*>> = mutableMapOf()

        fun <T : Screaper.Extractor.Task> add(kClass: KClass<T>, extractor: Screaper.Extractor<T>): ExtractorMap {
            extractors[kClass] = extractor
            return this
        }

        inline fun <reified T : Screaper.Extractor.Task> add(extractor: Screaper.Extractor<T>) =
            add(T::class, extractor)


        fun <T : Screaper.Extractor.Task> getFor(task: T): Screaper.Extractor<T> {
            @Suppress("UNCHECKED_CAST")
            return extractors.getValue(task::class) as Screaper.Extractor<T>
        }
    }

    private fun ScreaperRequest.copyWithMultipliedUrls(multiplier: Int) = if (multiplier > 0 && urls.size == 1) {
        val url = urls.single()
        val multipliedUrls = (1..multiplier).map {
            url.replace("(i)", it.toString())
        }
        copy(urls = multipliedUrls)
    } else {
        this
    }
}