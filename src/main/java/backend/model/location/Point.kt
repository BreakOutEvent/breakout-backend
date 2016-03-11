package backend.model.location

import javax.persistence.Embeddable

@Embeddable
class Point {

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    private constructor() {}

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }
}
