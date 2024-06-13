package screaper

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * result from screaper
 */
@Serializable
data class ScreaperResult(
    /**
     * each entry contains visited url and result of regexps
     */
    val entries: List<Entry>,
    val overallDuration: Duration?,
    /**
     * when whole task start
     */
    val overallStartTime: Instant,
) {
    @Serializable
    data class Entry(
        val url: String,
        val results: Map<String, List<String>>,
        val duration: Duration,
        val startTime: Instant,
        val error: String?,
    )
}

