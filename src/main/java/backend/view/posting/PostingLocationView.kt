package backend.view.posting

import backend.model.location.Location
import javax.validation.constraints.NotNull

class PostingLocationView() {

    @NotNull
    var latitude: Double? = null

    @NotNull
    var longitude: Double? = null

    var distance: Double? = null

    var locationData: Map<String, String> = mapOf()

    @NotNull
    var date: Long? = null

    constructor(location: Location) : this() {
        this.latitude = location.coord.latitude
        this.longitude = location.coord.longitude
        this.distance = location.distance
        this.date = location.date.toEpochSecond(java.time.ZoneOffset.UTC)
        this.locationData = location.locationData
    }
}