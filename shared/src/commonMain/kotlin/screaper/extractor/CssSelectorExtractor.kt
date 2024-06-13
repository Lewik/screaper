package screaper.extractor

import kotlinx.serialization.Serializable
import screaper.Screaper

class CssSelectorExtractor : Screaper.Extractor<CssSelectorExtractor.Task> {

    @Serializable
    data class Task(
        val cssSelector: String,
    ) : Screaper.Extractor.Task

    override suspend fun extract(task: Task, html: String) = ksoupParse(html, task.cssSelector)
}

expect fun ksoupParse(html: String, cssSelector: String): List<String>