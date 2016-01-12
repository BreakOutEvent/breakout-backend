package backend.model.misc

import javax.persistence.Embeddable

@Embeddable
class Coords(var latitude: Double = 0.0, var longitude: Double = 0.0)
