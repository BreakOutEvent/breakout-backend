package backend.model.event

import backend.model.misc.Coords
import java.time.LocalDateTime

interface EventService {
    fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coords, duration: Int): Event
    fun findAll(): Iterable<Event>
}
