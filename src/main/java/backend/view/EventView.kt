package backend.view

import backend.model.event.Event
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class EventView() {

    var id: Long? = null

    @NotNull
    var title: String? = null

    @NotNull
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
