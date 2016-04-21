package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.EventService
import backend.model.event.TeamService
import backend.model.location.Location
import backend.model.location.LocationRepository
import backend.model.location.Point
import backend.model.user.Participant
import backend.model.user.UserService
import backend.util.toLocalDateTime
import backend.view.LocationView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.validation.Valid

@RestController
@RequestMapping("/event/{eventId}/team/{teamId}/location")
open class LocationController {

    private val locationRepository: LocationRepository
    private val teamService: TeamService
    private val eventService: EventService
    private val userService: UserService

    @Autowired
    constructor(locationRepository: LocationRepository,
                teamService: TeamService,
                eventService: EventService,
                userService: UserService) {

        // TODO: Use locationService for database access
        this.locationRepository = locationRepository
        this.teamService = teamService
        this.eventService = eventService
        this.userService = userService
    }

    /**
     * GET /event/{eventId}/team/{teamId}/location/
     * Return a list of all locations for a certain team at a certain event
     */
    @RequestMapping("/")
    fun getAllLocations(@PathVariable("eventId") eventId: Long,
                             @PathVariable("teamId") teamId: Long): Iterable<LocationView> {

        // TODO: Only return those locations specified by eventId and teamId
        return locationRepository.findAll().map { LocationView(it) }
    }

    /**
     * POST /event/{eventId}/team/{teamId}/location/
     * Upload a new location for a specific team at a specific event
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/", method = arrayOf(POST))
    open fun createLocation(@PathVariable("eventId") eventId: Long,
                            @PathVariable("teamId") teamId: Long,
                            @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                            @Valid @RequestBody locationView: LocationView): LocationView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw UnauthorizedException("user is no participant")
        val team = teamService.getByID(eventId) ?: throw NotFoundException("no team with id $teamId found")
        if (!team.isMember(participant)) throw UnauthorizedException("user is not part of team $teamId are therefor cannot upload locations on it's behalf")

        val point = Point(locationView.latitude, locationView.longitude)
        val location = Location(point, participant, locationView.date.toLocalDateTime())
        val savedLocation = locationRepository.save(location)

        return LocationView(savedLocation)
    }

    /**
     * POST /event/{eventId}/team/{teamId}/location/multiple/
     * Upload multiple new locations for a specific team at a specific event
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/multiple/", method = arrayOf(POST))
    open fun createMultipleLocation(@PathVariable("eventId") eventId: Long,
                                    @PathVariable("teamId") teamId: Long,
                                    @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                                    @Valid @RequestBody locationViews: List<LocationView>): List<LocationView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw UnauthorizedException("user is no participant")
        val team = teamService.getByID(eventId) ?: throw NotFoundException("no team with id $teamId found")
        if (!team.isMember(participant)) throw UnauthorizedException("user is not part of team $teamId are therefor cannot upload locations on it's behalf")

        val savedLocations: MutableList<Location> = listOf<Location>() as MutableList<Location>

        //TODO: Move to service
        locationViews.forEach {
            val point = Point(it.latitude, it.longitude)

            val instant: Instant = Instant.ofEpochMilli(it.date);
            val date: LocalDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            val location = Location(point, participant, date)

            savedLocations.add(locationRepository.save(location))
        }

        return savedLocations.map { LocationView(it) }
    }

}
