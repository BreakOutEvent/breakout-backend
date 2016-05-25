package backend.model.location

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.util.distanceCoordsKM
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.*

@Entity
class Location : BasicEntity {

    @Embedded
    lateinit var coord: Coord
        private set

    @ManyToOne
    var uploader: Participant? = null

    @OneToOne(mappedBy = "location")
    var posting: Posting? = null
        private set

    @ManyToOne
    var team: Team? = null

    lateinit var date: LocalDateTime

    var distance: Double = 0.0

    @ElementCollection
    @MapKeyColumn(name = "location_data_key")
    @Column(name = "location_data_value")
    var locationData: Map<String, String> = mapOf()

    private constructor() : super()

    constructor(coord: Coord, uploader: Participant, date: LocalDateTime, locationData: Map<String, String>) {
        this.coord = coord
        this.team = uploader.currentTeam ?: throw DomainException("A user without a team can't upload locations")
        this.uploader = uploader
        this.date = date
        this.distance = distanceCoordsKM(from = team!!.event.startingLocation, to = coord)
        this.locationData = locationData
    }

    fun isDuringEvent(): Boolean {
        val minutes = ChronoUnit.MINUTES.between(this.team!!.event.date, this.date)
        if (minutes > 0 && minutes <= (this.team!!.event.duration * 60)) {
            return true
        }
        return false
    }
}
