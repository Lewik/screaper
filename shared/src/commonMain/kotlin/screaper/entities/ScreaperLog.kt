package screaper.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import screaper.db.WithId
import screaper.ScreaperResult

@Serializable
data class ScreaperLog(
    @SerialName("_id")
    @Contextual override val id: String,
    val result: ScreaperResult,
) : WithId