package backend.controller

import backend.model.event.EventService
import backend.model.misc.Coords
import backend.view.EventView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.map

@RestController
@RequestMapping("/event")
class EventController {

    val eventService: EventService

    @Autowired
    constructor(eventService: EventService) {
        this.eventService = eventService
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(
            value = "/",
            method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createEvent(@RequestBody body: EventView): EventView {

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
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getAllEvents(): Iterable<EventView> {
        return eventService.findAll().map { EventView(it) }
    }
}
