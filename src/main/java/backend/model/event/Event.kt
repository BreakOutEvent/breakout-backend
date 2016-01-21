package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.Coords
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Event() : BasicEntity() {

    lateinit var title: String
    lateinit var date: LocalDateTime
    lateinit var city: String

    @Embedded
    lateinit var startingLocation: Coords

    var duration: Int = 36

    constructor(title: String, date: LocalDateTime, city: String, startingLocation: Coords, duration: Int) : this() {
        this.title = title
        this.date = date
        this.city = city
        this.startingLocation = startingLocation
        this.duration = duration
    }

}
