package screaper.utils

import java.util.*

actual fun generateUuid(): Uuid = Uuid(UUID.randomUUID())
actual fun parseUuid(uuid: String): Uuid = Uuid(UUID.fromString(uuid))

actual typealias UuidPlatform = UUID
