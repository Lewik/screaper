package screaper

import kotlinx.serialization.Serializable

/**
 * Task for screaper
 */
@Serializable
data class ScreaperRequest(
    /**
     * list of urls to visit
     */
    val urls: List<String>,
    /**
     * tasks will be executed on each url
     */
    val tasks: Map<String, Screaper.Extractor.Task>,
) {
}

