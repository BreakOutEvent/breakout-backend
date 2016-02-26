package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.Coord
import java.time.LocalDateTime
import javax.persistence.Embedded
import javax.persistence.Entity

@Entity
class Event() : BasicEntity() {

    lateinit var title: String
    lateinit var date: LocalDateTime
    lateinit var city: String

    @Embedded
    lateinit var startingLocation: Coord

    var duration: Int = 36

    constructor(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int) : this() {
        this.title = title
        this.date = date
        this.city = city
        this.startingLocation = startingLocation
        this.duration = duration
    }

}
