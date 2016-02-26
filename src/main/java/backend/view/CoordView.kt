package backend.view

import backend.model.misc.Coord
import javax.validation.constraints.NotNull

class CoordView() {

    @NotNull
    var latitude: Double? = null

    @NotNull
    var longitude: Double? = null

    constructor(coord: Coord?) : this() {
        this.latitude = coord?.latitude
        this.longitude = coord?.longitude
    }

}
