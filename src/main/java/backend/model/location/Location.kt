package backend.model.location

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.posting.Posting
import backend.model.user.Participant
import java.time.LocalDateTime
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
class Location : BasicEntity {

    @Embedded
    lateinit var point: Point
        private set

    @ManyToOne
    var uploader: Participant? = null

    @OneToOne(mappedBy = "location")
    var posting: Posting? = null
        private set

    @ManyToOne
    var team: Team? = null

    lateinit var date: LocalDateTime

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(point: Point, uploader: Participant, date: LocalDateTime) {
        this.point = point
        this.team = uploader.currentTeam ?: throw DomainException("A user without a team can't upload locations")
        this.uploader = uploader
        this.date = date
    }

    fun toCoord() = Coord(latitude = this.point.latitude, longitude = this.point.longitude)
}
