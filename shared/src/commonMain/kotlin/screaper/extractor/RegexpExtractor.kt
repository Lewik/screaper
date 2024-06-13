package screaper.extractor

import kotlinx.serialization.Serializable
import screaper.Screaper


class RegexpExtractor : Screaper.Extractor<RegexpExtractor.Task> {
    @Serializable
    data class Task(
        val regexp: String,
    ) : Screaper.Extractor.Task

    override suspend fun extract(task: Task, html: String) =
        task.regexp.toRegex().findAll(html).map { it.value }.toList()
}