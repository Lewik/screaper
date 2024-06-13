package screaper.utils

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

expect fun generateUuid(): Uuid
expect fun parseUuid(uuid: String): Uuid

expect class UuidPlatform

@JvmInline
@Serializable
value class Uuid(
    @Contextual val value: UuidPlatform,
)
