package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.cache.CacheService
import backend.model.event.EventService
import backend.model.misc.Coord
import backend.view.EventView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.Valid

@RestController
@RequestMapping("/event")
open class EventController {

    open var eventService: EventService
    open var cacheService: CacheService
    private var logger: Logger

    @Autowired
    constructor(eventService: EventService, cacheService: CacheService) {
        this.eventService = eventService
        this.cacheService = cacheService
        this.logger = LoggerFactory.getLogger(EventController::class.java)
    }

    /**
     * POST /event/
     * Allows admin to create new event
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(CREATED)
    @RequestMapping("/", method = arrayOf(POST))
    open fun createEvent(@Valid @RequestBody body: EventView): EventView {

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
    @RequestMapping("/", method = arrayOf(GET))
    open fun getAllEvents(): Iterable<EventView> {
        return eventService.findAll().map(::EventView)
    }

    /**
     * GET /event/{id}/
     * Gets Event by ID
     */
    @RequestMapping("/{id}/", method = arrayOf(GET))
    open fun getEventById(@PathVariable("id") id: Long): EventView {
        val event = eventService.findById(id) ?: throw NotFoundException("event with id $id does not exist")
        return EventView(event)
    }

    /**
     * GET /event/{id}/posting/
     * Gets all Postings for given event
     */
    @RequestMapping("/{id}/posting/", method = arrayOf(GET))
    open fun getEventPostings(@PathVariable("id") id: Long): List<Long> {
        val postingIds = eventService.findPostingsById(id) ?: throw NotFoundException("event with id $id does not exist")
        return postingIds
    }

    /**
     * GET /event/{id}/distance/
     * Returns the sum of the distance of all teams of the event with {id}
     */
    @RequestMapping("/{id}/distance/", method = arrayOf(GET))
    open fun getEventDistance(@PathVariable("id") id: Long): Any {
        return cacheService.getCache("Event_${id}_Distance")
    }

    /**
     * GET /event/{id}/donatesum/
     * Returns the sum of the distance of all teams of the event with {id}
     */
    @RequestMapping("/{id}/donatesum/", method = arrayOf(GET))
    open fun getEventDonateSum(@PathVariable("id") id: Long): Any {
        return cacheService.getCache("Event_${id}_DonateSum")
    }
}
