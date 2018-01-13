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
import backend.util.speedToLocation
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class LocationServiceImpl(private val locationRepository: LocationRepository,
                          private val geoCodingService: GeoCodingService,
                          private val featureFlagService: FeatureFlagService,
                          private val eventService: EventService,
                          private val eventPublisher: ApplicationEventPublisher) : LocationService {

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

        val savedLocation = locationRepository.save(location)
        val team = location.team ?: throw Exception("Location has no team")
        //eventPublisher.publishEvent(LocationUploadedEvent(location, team))
        return savedLocation
    }

    @Transactional
    override fun adminCreate(coord: Coord, participant: Participant, date: LocalDateTime, doGeoCode: Boolean): Location {

        if (coord.latitude == 0.0 || coord.longitude == 0.0)
            throw BadRequestException("0.0, 0.0 locations not allowed")

        val locationData = when (doGeoCode) {
            true -> geoCodingService.getGeoCoded(coord)
            else -> mapOf()
        }

        val location = Location(coord, participant, date, locationData)
        location.isDuringEvent = true

        val savedLocation = locationRepository.save(location)
        val team = location.team ?: throw Exception("Location has no team")
        return savedLocation
    }

    private fun checkAndSetIsDuringEvent(location: Location, participant: Participant) {
        val teamHasStarted = participant.getCurrentTeam()?.hasStarted ?: throw DomainException("User has no team")

        // TODO: Move this logic to model layer!
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

    override fun generateSpeed() {
        val locationsToDo: Iterable<Location> = locationRepository.findBySpeedToLocationAndIsDuringEvent(null, true)
        for (location: Location in locationsToDo) {
            val priorLocation = findPriorLocation(location)
            if (priorLocation != null && priorLocation.isDuringEvent) {
                val speed = speedToLocation(priorLocation, location)
                if (speed != null) {
                    location.speedToLocation = speed
                    locationRepository.save(location)
                }
            }
        }
    }

    private fun findPriorLocation(location: Location): Location? =
            locationRepository.findByTeamIdAndPriorOrderByDateDesc(location.team?.id, location.id, location.date).firstOrNull()

}

class LocationUploadedEvent(val location: Location, val team: Team)
