package backend.Teamoverview

import backend.model.BasicEntity
import backend.model.event.Event
import backend.model.event.Team
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class TeamOverview : BasicEntity {

    private constructor() : super()

    constructor(team: Team, event: Event) {
        setOrUpdateValues(event, team)
    }

    fun setOrUpdateValues(event: Event, team: Team) {
        this.teamId = team.id!!
        this.teamName = team.name
        this.event = Event(event.id, event.title)
        this.members = team.members.map {
            TeamMember(it.id, it.firstname, it.lastname, it.emergencynumber, it.phonenumber)
        }
    }

    var teamId: Long = -1

    lateinit var teamName: String

    @Embedded
    var event: backend.Teamoverview.Event? = null

    @ElementCollection(fetch = FetchType.EAGER)
    var members: List<TeamMember> = listOf()

    @Embedded
    @AttributeOverrides(
            AttributeOverride(name = "latitude", column = Column(nullable = true)),
            AttributeOverride(name = "longitude", column = Column(nullable = true))
    )
    var lastLocation: LastLocation? = null

    @Embedded
    var lastPosting: LastPosting? = null

    @Embedded
    var lastContactWithHeadquarters: LastContactWithHeadquarters? = null

}

@Embeddable
class Event() {

    @Column(name = "event_id")
    var id: Long? = null
    var name: String? = null

    constructor(id: Long?, name: String?) : this() {
        this.id = id
        this.name = name
    }
}

@Embeddable
class TeamMember {

    constructor()

    var id: Long? = null
    var firstname: String? = null
    var lastname: String? = null
    var emergencyPhone: String? = null
    var contactPhone: String? = null

    constructor(id: Long?, firstname: String?, lastname: String?, emergencyPhone: String?, contactPhone: String?) {
        this.id = id
        this.firstname = firstname
        this.lastname = lastname
        this.emergencyPhone = emergencyPhone
        this.contactPhone = contactPhone
    }
}

@Embeddable
class LastLocation() {

    @Embedded
    var coord: Coord? = null

    @Column(name = "location_id")
    var id: Long? = null

    constructor(coord: Coord, id: Long) : this() {
        this.coord = coord
        this.id = id
    }
}

@Embeddable
class Coord() {

    var latitude: Double? = null
    var longitude: Double? = null

    constructor(latitude: Double, longitude: Double) : this() {
        this.latitude = latitude
        this.longitude = longitude
    }
}


@Embeddable
class LastPosting() {
    @Column(name = "posting_id")
    var id: Long? = null

    @Column(name = "posting_timestamp")
    var timestamp: LocalDateTime? = null

    constructor(id: Long?, timestamp: LocalDateTime) : this() {
        this.id = id
        this.timestamp = timestamp
    }
}

@Embeddable
class LastContactWithHeadquarters(var timestamp: LocalDateTime, var comment: String)
