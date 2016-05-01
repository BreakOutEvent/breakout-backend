package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.event.EventService
import backend.model.misc.Coord
import backend.util.distanceCoordsListKMfromStart
import backend.view.EventView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.Valid

@RestController
@RequestMapping("/event")
open class EventController {

    open var eventService: EventService

    @Autowired
    constructor(eventService: EventService) {
        this.eventService = eventService
    }

    /**
     * POST /event/
     * Allows admin to create new event
     */
    @PreAuthorize("hasRole('ADMIN')")
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
    @RequestMapping("/")
    open fun getAllEvents(): Iterable<EventView> {
        return eventService.findAll().map { EventView(it) }
    }

    /**
     * GET /event/{id}/posting/
     * Gets all Postings for given event
     */
    @RequestMapping("/{id}/posting/")
    open fun getEventPostings(@PathVariable("id") id: Long): List<Long> {
        val postingIds = eventService.findPostingsById(id) ?: throw NotFoundException("event with id $id does not exist")
        return postingIds
    }

    /**
     * GET /event/{id}/distance/
     * Returns the sum of the distance of all teams of the event with {id}
     */
    @RequestMapping("/{id}/distance/")
    open fun getEventDistance(@PathVariable("id") id: Long): Map<String, Double> {
        return eventService.getDistance(id)
    }
}
