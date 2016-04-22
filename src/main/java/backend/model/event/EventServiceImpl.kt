package backend.model.event

import backend.model.misc.Coord
import backend.model.posting.Posting
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventServiceImpl @Autowired constructor(val repository: EventRepository) : EventService {

    override fun exists(id: Long): Boolean {
        return this.repository.exists(id)
    }

    override fun findById(id: Long): Event? = repository.findById(id)

    override fun findAll(): Iterable<Event> = repository.findAll()

    override fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int): Event {
        val event = Event(title, date, city, startingLocation, duration)
        return repository.save(event)
    }

    override fun findPostingsById(id: Long): List<Long>? = repository.findPostingsById(id)

    // TODO: Verify that independently uploaded locations are found
    override fun findLocationPostingsById(id: Long): List<Posting>? = repository.findLocationPostingsById(id)

    override fun getPostingMaxDistanceById(id: Long): Posting? {
        val postingList = repository.getPostingMaxDistanceById(id)
        if (postingList.size <= 0) {
            return null
        } else {
            return postingList.first()
        }
    }
}
