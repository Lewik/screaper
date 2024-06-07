package org.screaper

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import screaper.ScreaperResult

@Serializable
data class ScreaperLog(
    @SerialName("_id")
    @Contextual val id: ObjectId?,
    val result: ScreaperResult,
)