package backend.controller

import backend.model.event.Event
import backend.model.event.EventService
import backend.model.misc.Coords
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

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

    @ExceptionHandler(Exception::class)
    fun handle(e: Exception) {
        e.printStackTrace()
    }
}

class EventView() {

    var id: Long? = null

    @NotNull
    var title: String? = null

    var date: Long? = null

    @NotNull
    var city: String? = null

    @Valid
    var startingLocation: Coords? = null

    var duration: Int = 36

    constructor(event: Event) : this() {
        this.id = event.id
        this.title = event.title
        this.date = event.date.toEpochSecond(ZoneOffset.UTC)
        this.city = event.city
        this.startingLocation = Coords()
        this.startingLocation!!.latitude = event.startingLocation.latitude
        this.startingLocation!!.longitude = event.startingLocation.longitude
        this.duration = event.duration
    }

    class Coords() {

        @NotNull
        var latitude: Double? = null

        @NotNull
        var longitude: Double? = null
    }
}
