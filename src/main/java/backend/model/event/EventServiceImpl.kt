package backend.model.event

import backend.model.misc.Coord
import backend.model.posting.Posting
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EventServiceImpl @Autowired constructor(val repository: EventRepository) : EventService {

    override fun exists(id: Long) = this.repository.exists(id)

    override fun findById(id: Long) = repository.findById(id)

    override fun findAll() = repository.findAll()

    @Transactional
    override fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int): Event {
        val event = Event(title, date, city, startingLocation, duration)
        return repository.save(event)
    }

    override fun findPostingsById(id: Long) = repository.findPostingsById(id)

    // TODO: Verify that independently uploaded locations are found
    override fun findLocationPostingsById(id: Long) = repository.findLocationPostingsById(id)

    override fun getPostingMaxDistanceById(id: Long): Posting? {
        val postingList = repository.getPostingMaxDistanceById(id)
        if (postingList.size <= 0) {
            return null
        } else {
            return postingList.first()
        }
    }
}
