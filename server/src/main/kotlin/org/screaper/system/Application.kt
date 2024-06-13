package org.screaper.system

import io.ktor.server.engine.*
import org.koin.core.context.startKoin
import org.koin.fileProperties


fun main() {

    val app = startKoin {
        fileProperties()
        modules(appModule)
    }


    app.koin
        .get<ApplicationEngine>()
        .start(wait = true)
}

