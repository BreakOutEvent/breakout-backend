package backend.model.location

import javax.persistence.Embeddable

@Embeddable
class Point {

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }
}
