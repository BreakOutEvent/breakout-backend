package backend.view

import backend.model.event.Event
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class EventView() {

    var id: Long? = null

    @NotNull
    @SafeHtml(whitelistType = NONE)
    lateinit var title: String

    @NotNull
    var date: Long? = null

    @NotNull
    @SafeHtml(whitelistType = NONE)
    lateinit var city: String

    @Valid
    lateinit var startingLocation: CoordView

    @JsonProperty("isCurrent")
    var isCurrent: Boolean = false

    @NotNull
    var duration: Int = 36

    constructor(event: Event) : this() {
        this.id = event.id
        this.title = event.title
        this.date = event.date.toEpochSecond(ZoneOffset.UTC)
        this.city = event.city
        this.startingLocation = CoordView(event.startingLocation)
        this.duration = event.duration
        this.isCurrent = event.isCurrent
    }
}
