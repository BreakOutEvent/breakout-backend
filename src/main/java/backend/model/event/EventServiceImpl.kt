package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.model.cache.CacheService
import backend.model.location.Location
import backend.model.misc.Coord
import backend.services.FeatureFlagService
import backend.util.data.DonateSums
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class EventServiceImpl @Autowired constructor(val repository: EventRepository,
                                              val teamService: TeamService,
                                              val cacheService: CacheService,
                                              val featureFlagService: FeatureFlagService) : EventService {

    val logger = LoggerFactory.getLogger(EventServiceImpl::class.java)

    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    fun scheduleRegenerateScores() {
        if (featureFlagService.isEnabled("event.scheduleRegenerateScores"))
            regenerateCache(null)
        logger.info("Recalculated event scores!"
        )
    }

    @Transactional
    override fun regenerateCache(eventId: Long?) {
        if (eventId != null) {
            updateCache(eventId)
        } else {
            if (featureFlagService.isEnabled("event.updateScores")) {
                findAll().forEach { event -> updateCache(event.id!!) }
            }
        }
    }

    fun updateCache(eventId: Long) {
        cacheService.updateCache("Event_${eventId}_Distance", mapOf("distance" to getDistance(eventId)))
        cacheService.updateCache("Event_${eventId}_DonateSum", getDonateSum(eventId))
        cacheService.updateCache("Event_${eventId}_HighScore", teamService.findByEventId(eventId).map {
            mapOf(
                    "eventId" to eventId,
                    "teamId" to it.id,
                    "teamName" to it.name,
                    "distance" to teamService.getDistance(it.id!!),
                    "donatedSum" to teamService.getDonateSum(it.id!!)
            )
        })
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

        return event.teams.parallelStream().map(Team::getCurrentDistance).reduce { acc: Double, distance: Double ->
            acc + distance
        }.orElseGet { 0.0 }
    }

    override fun getDonateSum(id: Long): DonateSums {
        val event = this.findById(id) ?: throw NotFoundException("event with id $id does not exist")

        val donateSumTeams = event.teams.parallelStream().map { team ->
            val donateSum = teamService.getDonateSum(team.id!!)
            DonateSums(donateSum.sponsorSum, donateSum.challengeSum, donateSum.fullSum)
        }

        return donateSumTeams.parallel().reduce { accSums: DonateSums, donateSums: DonateSums ->
            DonateSums(accSums.sponsorSum + donateSums.sponsorSum,
                    accSums.challengeSum + donateSums.challengeSum,
                    accSums.fullSum + donateSums.fullSum)
        }.orElseGet { DonateSums(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO) }
    }
}
