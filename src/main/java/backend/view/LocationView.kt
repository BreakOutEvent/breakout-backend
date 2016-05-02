package backend.view

import backend.model.location.Location
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZoneOffset
import javax.validation.constraints.NotNull

class LocationView {

    @NotNull
    val latitude: Double

    @NotNull
    val longitude: Double

    var distance: Double? = null

    var team: String? = null

    var teamId: Long? = null

    var event: String? = null

    var eventId: Long? = null

    @NotNull
    val date: Long

    constructor(location: Location) {
        this.latitude = location.coord.latitude
        this.longitude = location.coord.longitude
        this.distance = location.distance
        this.date = location.date.toEpochSecond(ZoneOffset.UTC)

        this.team = location.team?.name
        this.teamId = location.team?.id

        this.event = location.team?.event?.title
        this.eventId = location.team?.event?.id
    }

    @JsonCreator
    constructor(@JsonProperty("latitude") latitude: Double, @JsonProperty("longitude") longitude: Double, @JsonProperty("date") date: Long) {
        this.latitude = latitude
        this.longitude = longitude
        this.date = date
    }
}
