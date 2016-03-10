package backend.model.location

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.user.Participant
import javax.persistence.*
import javax.persistence.CascadeType.PERSIST

@Entity
class Location : BasicEntity {

    @Embedded
    lateinit var point: Point
        private set

    @OneToOne(cascade = arrayOf(PERSIST))
    lateinit var uploader: Participant
        private set

    @ManyToOne
    lateinit var team: Team
        private set

    /**
     * no args constructor for orm
     */
    constructor(): super()

    constructor(point: Point, uploader: Participant) {
        this.point = point
        this.team = uploader.currentTeam ?: throw DomainException("A user without a team can't upload locations")
        this.uploader = uploader
    }
}

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
