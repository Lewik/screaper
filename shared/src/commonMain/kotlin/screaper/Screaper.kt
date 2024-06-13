package screaper


/**
 * interface for future screapers
 */
interface Screaper {

    suspend fun screap(_request: ScreaperRequest, multiplier: Int? = null): ScreaperResult


}