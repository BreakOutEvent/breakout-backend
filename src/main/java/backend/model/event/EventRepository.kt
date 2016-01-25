package backend.model.event

import org.springframework.data.repository.CrudRepository

interface EventRepository : CrudRepository<Event, Long> {
    fun findByCity(city: String): Iterable<Event>
    fun findById(id: Long): Event?
}
