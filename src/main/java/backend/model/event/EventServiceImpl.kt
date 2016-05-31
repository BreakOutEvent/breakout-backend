package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.model.location.Location
import backend.model.misc.Coord
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

        val teamDistances = event.teams.map { teamService.getDistance(it.id!!) }
        val actualDistance = teamDistances.sumByDouble { it["actual_distance"]!! }
        val linearDistance = teamDistances.sumByDouble { it["linear_distance"]!! }

        return mapOf(
                "actual_distance" to actualDistance,
                "linear_distance" to linearDistance)

    }

    override fun getDonateSum(id: Long): Map<String, BigDecimal> {
        val event = this.findById(id) ?: throw NotFoundException("event with id $id does not exist")

        var sponsorSum = BigDecimal.ZERO
        var withProofSum = BigDecimal.ZERO
        var acceptedProofSum = BigDecimal.ZERO
        var fullSum = BigDecimal.ZERO

        event.teams.forEach { team ->
            val donateSum = teamService.getDonateSum(team.id!!)
            sponsorSum = sponsorSum.add(donateSum["sponsoring_sum"]!!)
            withProofSum = withProofSum.add(donateSum["challenges_with_proof_sum"]!!)
            acceptedProofSum = acceptedProofSum.add(donateSum["challenges_accepted_proof_sum"]!!)
            fullSum = fullSum.add(donateSum["full_sum"]!!)
        }

        sponsorSum = sponsorSum.setScale(2, BigDecimal.ROUND_HALF_UP)
        withProofSum = withProofSum.setScale(2, BigDecimal.ROUND_HALF_UP)
        acceptedProofSum = acceptedProofSum.setScale(2, BigDecimal.ROUND_HALF_UP)
        fullSum = fullSum.setScale(2, BigDecimal.ROUND_HALF_UP)

        return mapOf(
                "sponsoring_sum" to sponsorSum,
                "challenges_with_proof_sum" to withProofSum,
                "challenges_accepted_proof_sum" to acceptedProofSum,
                "full_sum" to fullSum)
    }
}
