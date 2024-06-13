package screaper.extractor

import kotlinx.serialization.Serializable

interface Extractor<T : Extractor.Task> {
    @Serializable
    sealed interface Task

    suspend fun extract(task: T, html: String): List<String>
}