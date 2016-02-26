package backend.model.event

import backend.model.misc.Coord
import java.time.LocalDateTime

interface EventService {
    fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int): Event
    fun findAll(): Iterable<Event>
}
