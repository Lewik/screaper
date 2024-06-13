package screaper.extractor

import kotlinx.serialization.Serializable


class RegexpExtractor : Extractor<RegexpExtractor.Task> {
    @Serializable
    data class Task(
        val regexp: String,
    ) : Extractor.Task

    override suspend fun extract(task: Task, html: String) =
        task.regexp.toRegex().findAll(html).map { it.value }.toList()
}