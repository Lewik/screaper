package screaper


/**
 * interface for future screapers
 */
interface Screaper {

    suspend fun screap(_request: ScreaperRequest, multiplier: Int? = null): ScreaperResult

    interface Extractor<T: Extractor.Task> {
        interface Task
        suspend fun extract(task: T, html: String): List<String>
    }
}