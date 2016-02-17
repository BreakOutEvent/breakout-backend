package backend.controller

import backend.model.event.EventService
import backend.model.misc.Coords
import backend.view.EventView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.Valid

@RestController
@RequestMapping("/event")
open class EventController {

    private var eventService: EventService

    @Autowired
    constructor(eventService: EventService) {
        this.eventService = eventService
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(CREATED)
    @RequestMapping(value = "/", method = arrayOf(POST))
    open fun createEvent(@Valid @RequestBody body: EventView): EventView {

        val event = eventService.createEvent(
                title = body.title!!,
                date = LocalDateTime.ofEpochSecond(body.date!!, 0, ZoneOffset.UTC),
                city = body.city!!,
                duration = body.duration,
                startingLocation = Coords(body.startingLocation!!.latitude!!, body.startingLocation!!.longitude!!))

        return EventView(event)
    }

    @RequestMapping(
            value = "/",
            method = arrayOf(GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    open fun getAllEvents(): Iterable<EventView> {
        return eventService.findAll().map { EventView(it) }
    }
}
