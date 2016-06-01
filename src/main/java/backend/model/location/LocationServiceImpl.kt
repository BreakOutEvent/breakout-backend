package backend.model.location

import backend.controller.exceptions.BadRequestException
import backend.exceptions.DomainException
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.services.FeatureFlagService
import backend.services.GeoCodingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class LocationServiceImpl : LocationService {

    private val locationRepository: LocationRepository
    private val geoCodingService: GeoCodingService
    private val featureFlagService: FeatureFlagService

    @Autowired
    constructor(locationRepository: LocationRepository,
                geoCodingService: GeoCodingService,
                featureFlagService: FeatureFlagService) {

        this.locationRepository = locationRepository
        this.geoCodingService = geoCodingService
        this.featureFlagService = featureFlagService
    }

    override fun findAll(): Iterable<Location> {
        return locationRepository.findAll()
    }

    override fun save(location: Location): Location {
        return locationRepository.save(location)
    }

    override fun create(coord: Coord, participant: Participant, date: LocalDateTime): Location {
        return create(coord, participant, date, false)
    }

    @Transactional
    override fun create(coord: Coord, participant: Participant, date: LocalDateTime, doGeoCode: Boolean): Location {

        if (coord.latitude == 0.0 || coord.longitude == 0.0)
            throw BadRequestException("0.0, 0.0 locations not allowed")

        val locationData = if (doGeoCode) geoCodingService.getGeoCoded(coord) else mapOf()
        val location = Location(coord, participant, date, locationData)

        checkAndSetIsDuringEvent(location, participant)

        return locationRepository.save(location)
    }

    private fun checkAndSetIsDuringEvent(location: Location, participant: Participant) {
        val teamHasStarted = participant.currentTeam?.hasStarted ?: throw DomainException("User has no team")

        if (featureFlagService.isEnabled("event.isNow") && teamHasStarted) {
            location.isDuringEvent = true
        }
    }

    override fun findByTeamId(id: Long): Iterable<Location> {
        return locationRepository.findByTeamId(id)
    }

    override fun findByEventId(id: Long): Iterable<Location> {
        return locationRepository.findByEventId(id)
    }

}

