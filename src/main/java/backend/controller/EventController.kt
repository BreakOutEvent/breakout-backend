package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.exceptions.CacheNonExistentException
import backend.model.cache.CacheService
import backend.model.event.EventService
import backend.model.misc.Coord
import backend.util.CacheNames.LOCATIONS
import backend.view.EventView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.Valid

@RestController
@RequestMapping("/event")
class EventController(open var eventService: EventService,
                      open var cacheService: CacheService) {

    private var logger: Logger = LoggerFactory.getLogger(EventController::class.java)

    /**
     * POST /event/
     * Allows admin to create new event
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(CREATED)
    @PostMapping("/")
    @CacheEvict(LOCATIONS, allEntries = true)
    fun createEvent(@Valid @RequestBody body: EventView): EventView {

        val event = eventService.createEvent(
                title = body.title,
                date = LocalDateTime.ofEpochSecond(body.date!!, 0, ZoneOffset.UTC),
                city = body.city,
                duration = body.duration,
                startingLocation = Coord(body.startingLocation.latitude!!, body.startingLocation.longitude!!))

        return EventView(event)
    }

    /**
     * GET /event/
     * Gets a list of all events
     */
    @GetMapping("/")
    fun getAllEvents(): Iterable<EventView> {
        return eventService.findAll().map(::EventView)
    }

    /**
     * GET /event/{id}/
     * Gets Event by ID
     */
    @GetMapping("/{id}/")
    fun getEventById(@PathVariable("id") id: Long): EventView {
        val event = eventService.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        return EventView(event)
    }

    /**
     * GET /event/{id}/posting/
     * Gets all Postings for given event
     */
    @GetMapping("/{id}/posting/")
    fun getEventPostings(@PathVariable("id") id: Long): List<Long> {
        return eventService.findPostingsById(id) ?: throw NotFoundException("event with id $id does not exist")
    }

    /**
     * GET /event/{id}/distance/
     * Returns the sum of the distance of all teams of the event with {id}
     */
    @GetMapping("/{id}/distance/")
    open fun getEventDistance(@PathVariable("id") id: Long): Any {
        try {
            return cacheService.getCache("Event_${id}_Distance")
        } catch (e: CacheNonExistentException) {
            eventService.regenerateCache(id)
            throw e
        }
    }

    /**
     * GET /event/{id}/donatesum/
     * Returns the sum of the distance of all teams of the event with {id}
     */
    @GetMapping("/{id}/donatesum/")
    open fun getEventDonateSum(@PathVariable("id") id: Long): Any {
        try {
            return cacheService.getCache("Event_${id}_DonateSum")
        } catch (e: CacheNonExistentException) {
            eventService.regenerateCache(id)
            throw e
        }
    }

    /*
     * GET /event/{id}/highscore/
     * Calculates and returns the team ranking of the event with {id}
     */
    @GetMapping("/{id}/highscore/")
    open fun getEventHighscore(@PathVariable("id") id: Long): Any {
        try {
            return cacheService.getCache("Event_${id}_HighScore")
        } catch (e: CacheNonExistentException) {
            eventService.regenerateCache(id)
            throw e
        }
    }
}
