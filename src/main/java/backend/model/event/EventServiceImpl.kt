package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.model.location.Location
import backend.model.misc.Coord
import backend.util.distanceCoordsListKMfromStart
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class EventServiceImpl @Autowired constructor(val repository: EventRepository, val teamService: TeamService) : EventService {

    override fun exists(id: Long) = this.repository.exists(id)

    override fun findById(id: Long) = repository.findById(id)

    override fun findAll() = repository.findAll()

    @Transactional
    override fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int): Event {
        val event = Event(title, date, city, startingLocation, duration)
        return repository.save(event)
    }

    override fun findPostingsById(id: Long) = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long) = repository.findLocationPostingsById(id)

    override fun getLocationMaxDistanceByIdEachTeam(id: Long): List<Location> = repository.getLocationMaxDistanceByIdEachTeam(id)

    override fun getDistance(id: Long): Map<String, Double> {
        val event = this.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        val locations = this.findLocationPostingsById(id)

        // Distance calculated with from all uploaded calculations, including steps in between (e.g A -> B -> C)
        val actualDistance = distanceCoordsListKMfromStart(event.startingLocation, locations.map { it.coord })

        val postingDistances = this.getLocationMaxDistanceByIdEachTeam(id)
        val linearDistance = postingDistances.sumByDouble { it.distance }
        return mapOf("actual_distance" to actualDistance, "linear_distance" to linearDistance)
    }

    override fun getDonateSum(id: Long): Map<String, BigDecimal> {
        val event = this.findById(id) ?: throw NotFoundException("event with id $id does not exist")

        val sponsorSum = BigDecimal.ZERO
        val withProofSum = BigDecimal.ZERO
        val acceptedProofSum = BigDecimal.ZERO
        val fullSum = BigDecimal.ZERO

        event.teams.forEach { team ->
            val donateSum = teamService.getDonateSum(team.id!!)
            sponsorSum.add(donateSum["sponsoring_sum"]!!)
            withProofSum.add(donateSum["challenges_with_proof_sum"]!!)
            acceptedProofSum.add(donateSum["challenges_accepted_proof_sum"]!!)
            fullSum.add(donateSum["full_sum"]!!)
        }

        return mapOf(
                "sponsoring_sum" to sponsorSum,
                "challenges_with_proof_sum" to withProofSum,
                "challenges_accepted_proof_sum" to acceptedProofSum,
                "full_sum" to fullSum)
    }
}
