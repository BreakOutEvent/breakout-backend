package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.model.cache.CacheService
import backend.model.location.Location
import backend.model.misc.Coord
import backend.util.data.DonateSums
import backend.util.parallelStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class EventServiceImpl @Autowired constructor(val repository: EventRepository,
                                              val teamService: TeamService,
                                              val cacheService: CacheService) : EventService {

    @Transactional
    override fun regenerateCache() {
        findAll().forEach { event ->
            cacheService.updateCache("Event_${event.id}_Distance", mapOf("distance" to getDistance(event.id!!)))
            cacheService.updateCache("Event_${event.id}_DonateSum", getDonateSum(event.id!!))
        }
    }

    override fun exists(id: Long) = this.repository.exists(id)

    override fun findById(id: Long) = repository.findById(id)

    override fun findAll(): Iterable<Event> = repository.findAll()

    @Transactional
    override fun createEvent(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int): Event {
        val event = Event(title, date, city, startingLocation, duration)
        return repository.save(event)
    }

    override fun findPostingsById(id: Long) = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long) = repository.findLocationPostingsById(id)

    override fun getLocationMaxDistanceByIdEachTeam(id: Long): List<Location> = repository.getLocationMaxDistanceByIdEachTeam(id)

    override fun getDistance(id: Long): Double {
        val event = this.findById(id) ?: throw NotFoundException("event with id $id does not exist")

        return event.teams.parallelStream().map { team ->
            teamService.getDistance(team.id!!)
        }.reduce { acc: Double, distance: Double ->
            acc + distance
        }.orElseGet { 0.0 }
    }

    override fun getDonateSum(id: Long): DonateSums {
        val event = this.findById(id) ?: throw NotFoundException("event with id $id does not exist")

        val donateSumTeams = event.teams.parallelStream().map { team ->
            val donateSum = teamService.getDonateSum(team.id!!)
            return@map DonateSums(donateSum.sponsorSum,
                    donateSum.withProofSum,
                    donateSum.acceptedProofSum,
                    donateSum.fullSum)
        }

        return donateSumTeams.parallel().reduce { accSums: DonateSums, donateSums: DonateSums ->
            DonateSums(accSums.sponsorSum + donateSums.sponsorSum,
                    accSums.withProofSum + donateSums.withProofSum,
                    accSums.acceptedProofSum + donateSums.acceptedProofSum,
                    accSums.fullSum + donateSums.fullSum)
        }.orElseGet { DonateSums(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO) }
    }
}
