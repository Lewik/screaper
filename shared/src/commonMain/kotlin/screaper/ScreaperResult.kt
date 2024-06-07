package screaper

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
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
    val overallStartTime: Instant?,
) {
    @Serializable
    data class Entry(
        val url: Url,
        val results: Map<Name, Value>,
        val duration: Duration,
        val startTime: Instant,
        val error: String?
    ) {
        /**
         * value class - kotlin feature for typing primitives without runtime slow (checked in compile time)
         */
        @Serializable
        @JvmInline
        value class Url(val s: String)


        @Serializable
        @JvmInline
        value class Name(val s: String)

        @Serializable
        @JvmInline
        value class Value(val s: String?)
    }
}

