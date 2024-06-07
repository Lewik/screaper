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
     * regexps will be executed on each url
     * key - label of regexp
     * value - regexp to execute
     */
    val regexPatterns: Map<String, String>,
)
