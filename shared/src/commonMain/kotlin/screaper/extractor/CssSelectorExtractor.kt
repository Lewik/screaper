package screaper.extractor

import kotlinx.serialization.Serializable

class CssSelectorExtractor : Extractor<CssSelectorExtractor.Task> {

    @Serializable
    data class Task(
        val cssSelector: String,
    ) : Extractor.Task

    override suspend fun extract(task: Task, html: String) = ksoupParse(html, task.cssSelector)
}

expect fun ksoupParse(html: String, cssSelector: String): List<String>