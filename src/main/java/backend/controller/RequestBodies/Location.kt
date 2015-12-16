@file:JvmName("Location")

package backend.controller.RequestBodies

import javax.validation.constraints.NotNull

class Location() {
    @NotNull var lat: Double? = null
    @NotNull var lon: Double? = null
}
