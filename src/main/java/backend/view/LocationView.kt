package backend.view

import backend.model.location.Location
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

class LocationView {

    @NotNull
    val latitude: Double

    @NotNull
    val longitude: Double


    constructor(location: Location) {
        this.latitude = location.point.latitude
        this.longitude = location.point.longitude
    }

    @JsonCreator
    constructor(@JsonProperty("latitude") latitude: Double, @JsonProperty("longitude") longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }
}
