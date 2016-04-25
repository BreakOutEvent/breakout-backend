package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.model.location.Location
import backend.model.misc.Coord
import backend.model.posting.Posting
import backend.util.distanceCoordsListKMfromStart
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

    override fun getLocationMaxDistanceByIdEachTeam(id: Long): List<Location> = repository.getLocationMaxDistanceByIdEachTeam(id)

    override fun getDistance(id: Long): Map<String, Double> {
        val event = this.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        val locations = this.findLocationPostingsById(id)

        // Distance calculated with from all uploaded calculations, including steps in between (e.g A -> B -> C)
        val actualDistance = distanceCoordsListKMfromStart(event.startingLocation, locations.map { it.coord })

        val postingDistances = this.getLocationMaxDistanceByIdEachTeam(id)
        var linearDistance = postingDistances.sumByDouble { it.distance ?: 0.0 }
        return mapOf("actual_distance" to actualDistance, "linear_distance" to linearDistance)
    }

}
