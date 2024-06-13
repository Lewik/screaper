package screaper

import kotlinx.serialization.Serializable
import screaper.extractor.Extractor

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
     val tasks: Map<String, Extractor.Task>,
) {
}

