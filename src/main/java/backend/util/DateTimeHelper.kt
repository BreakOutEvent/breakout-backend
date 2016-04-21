package backend.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Convert milliseconds to LocalDateTime
 */
fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.ofEpochMilli(this)
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}
