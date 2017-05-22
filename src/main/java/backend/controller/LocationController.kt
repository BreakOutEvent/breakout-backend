package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.TeamService
import backend.model.location.Location
import backend.model.location.LocationService
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.UserService
import backend.util.CacheNames.LOCATIONS
import backend.util.CacheNames.POSTINGS
import backend.util.localDateTimeOf
import backend.util.speedToLocation
import backend.view.BasicLocationView
import backend.view.LocationView
import backend.view.TeamLocationView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/event/{eventId}")
class LocationController(private val locationService: LocationService,
                         private val teamService: TeamService,
                         private val userService: UserService) {

    private val logger: Logger = LoggerFactory.getLogger(LocationController::class.java)


    /**
     * GET /event/{eventId}/location/
     * Return a list of all locations for a specific event
     */
    @Cacheable(LOCATIONS, sync = true)
    @GetMapping("/location/")
    fun getAllLocationsForEvent(@PathVariable eventId: Long,
                                @RequestParam(value = "perTeam", required = false) perTeam: Int?): Iterable<TeamLocationView> {
        return locationService.findByEventId(eventId, perTeam ?: 20).map { data ->
            TeamLocationView(data.key, data.value)
        }
    }

    /**
     * GET /event/{eventId}/team/{teamId}/location/
     * Return a list of all locations for a certain team at a certain event
     */
    @GetMapping("/team/{teamId}/location/")
    fun getAllLocationsForEventAndTeam(@PathVariable("eventId") eventId: Long,
                                       @PathVariable("teamId") teamId: Long,
                                       @RequestParam(value = "perTeam", required = false) perTeam: Int?): Iterable<BasicLocationView> {
        val teamLocations = locationService.findByTeamId(teamId, perTeam ?: 20)


        val locationPairs: MutableList<Pair<Location, Location>> = arrayListOf()

        var lastLocation: Location? = null
        teamLocations.forEach { thisLocation ->
            if (lastLocation != null) {
                locationPairs.add(Pair(lastLocation!!, thisLocation))
            }
            lastLocation = thisLocation
        }

        val speedToLocation = locationPairs.map { (first, second) ->
            Pair(speedToLocation(first, second), second)
        }

        return teamLocations.map { location ->
            BasicLocationView(location, speedToLocation.find { it.second.id == location.id }?.first)
        }

    }

    /**
     * POST /event/{eventId}/team/{teamId}/location/
     * Upload a new location for a specific team at a specific event
     */
    @Caching(evict = arrayOf(CacheEvict(POSTINGS, allEntries = true), CacheEvict(LOCATIONS, allEntries = true)))
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/team/{teamId}/location/")
    @ResponseStatus(CREATED)
    fun createLocation(@PathVariable("eventId") eventId: Long,
                       @PathVariable("teamId") teamId: Long,
                       @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                       @Valid @RequestBody locationView: LocationView): LocationView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw UnauthorizedException("user is no participant")
        val team = teamService.findOne(teamId) ?: throw NotFoundException("no team with id $teamId found")
        if (!team.isMember(participant)) throw UnauthorizedException("user is not part of team $teamId are therefor cannot upload locations on it's behalf")

        val coord = Coord(locationView.latitude, locationView.longitude)
        val location = locationService.create(coord, participant, localDateTimeOf(epochSeconds = locationView.date))

        return LocationView(location)
    }

    /**
     * POST /event/{eventId}/team/{teamId}/location/multiple/
     * Upload multiple new locations for a specific team at a specific event
     */
    @Caching(evict = arrayOf(CacheEvict(POSTINGS, allEntries = true), CacheEvict(LOCATIONS, allEntries = true)))
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/team/{teamId}/location/multiple/")
    @ResponseStatus(CREATED)
    fun createMultipleLocation(@PathVariable("eventId") eventId: Long,
                               @PathVariable("teamId") teamId: Long,
                               @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                               @Valid @RequestBody locationViews: List<LocationView>): List<LocationView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw UnauthorizedException("user is no participant")
        val team = teamService.findOne(teamId) ?: throw NotFoundException("no team with id $teamId found")
        if (!team.isMember(participant)) throw UnauthorizedException("user is not part of team $teamId are therefor cannot upload locations on it's behalf")

        val savedLocationsAsLocationViews = locationViews.map {
            val coord = Coord(it.latitude, it.longitude)
            val savedLocation = locationService.create(coord, participant, localDateTimeOf(epochSeconds = it.date))
            LocationView(savedLocation)
        }

        return savedLocationsAsLocationViews
    }

}
