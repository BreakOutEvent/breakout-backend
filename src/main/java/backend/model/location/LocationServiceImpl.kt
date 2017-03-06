package backend.model.location

import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.model.event.EventService
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.services.FeatureFlagService
import backend.services.GeoCodingService
import backend.util.parallelStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class LocationServiceImpl : LocationService {

    private val locationRepository: LocationRepository
    private val geoCodingService: GeoCodingService
    private val featureFlagService: FeatureFlagService
    private val eventService: EventService

    @Autowired
    constructor(locationRepository: LocationRepository,
                geoCodingService: GeoCodingService,
                featureFlagService: FeatureFlagService,
                eventService: EventService) {

        this.locationRepository = locationRepository
        this.geoCodingService = geoCodingService
        this.featureFlagService = featureFlagService
        this.eventService = eventService
    }

    override fun findAll(): Iterable<Location> {
        return locationRepository.findAll()
    }

    override fun save(location: Location): Location {
        return locationRepository.save(location)
    }

    @Transactional
    override fun create(coord: Coord, participant: Participant, date: LocalDateTime, doGeoCode: Boolean): Location {

        if (coord.latitude == 0.0 || coord.longitude == 0.0)
            throw BadRequestException("0.0, 0.0 locations not allowed")

        val locationData = when (doGeoCode) {
            true -> geoCodingService.getGeoCoded(coord)
            else -> mapOf()
        }

        val location = Location(coord, participant, date, locationData)

        checkAndSetIsDuringEvent(location, participant)

        return locationRepository.save(location)
    }

    private fun checkAndSetIsDuringEvent(location: Location, participant: Participant) {
        val teamHasStarted = participant.getCurrentTeam()?.hasStarted ?: throw DomainException("User has no team")

        if (featureFlagService.isEnabled("event.isNow") && teamHasStarted) {
            location.isDuringEvent = true
        }
    }

    override fun findByTeamId(id: Long, perTeam: Int): Iterable<Location> {
        val first = locationRepository.findTeamLocationBounds(id).firstOrNull()
        if (first != null) {
            try {
                val test = first as Array<*>
                val locations = locationRepository.findByTeamId(id, (test[0] as Number).toLong(), (test[1] as Number).toLong(), (test[2] as Number).toLong(), perTeam)
                return locations.filter { it.isDuringEvent }
            } catch (e: Exception) {
                return emptyList()
            }
        } else {
            return emptyList()
        }
    }

    override fun findByEventId(id: Long, perTeam: Int): Map<Team, Iterable<Location>> {
        val event = eventService.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        return event.teams.filter(Team::hasStarted).parallelStream().map { team ->
            val locations = this.findByTeamId(team.id!!, perTeam)
            return@map mutableMapOf(team to locations)
        }.reduce { acc: MutableMap<Team, Iterable<Location>>, team: Map<Team, Iterable<Location>> ->
            acc.putAll(team.filter { it.value.toList().isNotEmpty() })
            return@reduce acc
        }.orElseGet { mutableMapOf<Team, Iterable<Location>>() }
    }

}

