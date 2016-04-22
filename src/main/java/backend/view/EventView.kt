package backend.view

import backend.model.event.Event
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class EventView() {

    var id: Long? = null

    @NotNull
    lateinit var title: String

    @NotNull
    var date: Long? = null

    @NotNull
    lateinit var city: String

    @Valid
    lateinit var startingLocation: CoordView

    @NotNull
    var duration: Int = 36

    constructor(event: Event) : this() {
        this.id = event.id
        this.title = event.title
        this.date = event.date.toEpochSecond(ZoneOffset.UTC)
        this.city = event.city
        this.startingLocation = CoordView(event.startingLocation)
        this.duration = event.duration
    }
}
