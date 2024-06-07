package screaper

/**
 * interface for future screapers
 */
interface Screaper {
    suspend fun screap(request: ScreaperRequest): ScreaperResult
}