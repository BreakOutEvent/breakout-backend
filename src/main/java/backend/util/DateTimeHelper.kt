package backend.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Convert epochSeconds to LocalDateTime
 */
fun localDateTimeOf(epochSeconds: Long): LocalDateTime {
    val instant = Instant.ofEpochSecond(epochSeconds)
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}
