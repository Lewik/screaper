package org.screaper.system

import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.server.engine.*
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.screaper.db.ScreaperLogDataRepository
import screaper.CoroutineScreaper
import screaper.Screaper
import screaper.extractor.CssSelectorExtractor
import screaper.extractor.RegexpExtractor


val appModule = module {

    single {
        MongoClient.create(connectionString = getProperty<String>("mongo_connection_string"))
    }

    singleOf(::ScreaperLogDataRepository)

    single {
        HttpClient {
            expectSuccess = true
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }
    }

    single<Screaper> {
        CoroutineScreaper(
            client = get(),
            extractorMap = CoroutineScreaper.ExtractorMap()
                .add(CssSelectorExtractor())
                .add(RegexpExtractor()),
            screaperLogDataRepository = get<ScreaperLogDataRepository>()
        )
    }


    factory<ApplicationEngine> {
        ktorServer(
            get(),
            get(),
        )
    }
}

inline fun <reified T : ArrayList<Int>> Module.singleRepo(
    noinline definition: Definition<T>,
) {
    single(named("repo${T::class.simpleName}"), definition = definition)
}




