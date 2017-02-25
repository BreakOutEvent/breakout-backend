package backend.view

import backend.model.location.Location
import java.time.ZoneOffset
import javax.validation.constraints.NotNull

class BasicLocationView {

    var id: Long? = null

    @NotNull
    val latitude: Double

    @NotNull
    val longitude: Double

    var distance: Double? = null

    var locationData: Map<String, String> = mapOf()

    var isDuringEvent: Boolean? = null

    @NotNull
    val date: Long

    constructor(location: Location) {
        this.id = location.id

        this.latitude = location.coord.latitude
        this.longitude = location.coord.longitude
        this.distance = location.distance
        this.date = location.date.toEpochSecond(ZoneOffset.UTC)
        this.isDuringEvent = location.isDuringEvent

        this.locationData = location.locationData
    }
}
