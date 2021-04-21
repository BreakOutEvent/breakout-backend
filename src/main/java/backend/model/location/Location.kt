package backend.model.location

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.util.distanceCoordsKM
import backend.util.distanceCoordsListKM
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Location : BasicEntity {

    @Embedded
    lateinit var coord: Coord
        private set

    @ManyToOne(fetch = FetchType.LAZY)
    var uploader: Participant? = null

    @OneToOne(mappedBy = "location", fetch = FetchType.LAZY)
    var posting: Posting? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var team: Team? = null

    var isDuringEvent: Boolean = false

    lateinit var date: LocalDateTime

    var distance: Double = 0.0

    @Embedded
    var speedToLocation: SpeedToLocation? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "location_data_key")
    @Column(name = "location_data_value")
    var locationData: Map<String, String> = mapOf()

    private constructor() : super()

    constructor(coord: Coord, uploader: Participant, date: LocalDateTime, locationData: Map<String, String>) {
        this.coord = coord
        this.team = uploader.getCurrentTeam() ?: throw DomainException("A user without a team can't upload locations")
        this.uploader = uploader
        this.date = date
        this.distance = distanceCoordsKM(from = team!!.event.startingLocation, to = coord)
        if (team!!.event.city == "Anywhere") {
            this.distance = distanceCoordsListKM(this.team!!.locations.map{Coord(it.coord.latitude, it.coord.longitude)} + coord)
        }
        this.locationData = locationData
    }

}

class SpeedToLocation(val speed: Double? = null, val secondsDifference: Long? = null, val distanceKm: Double? = null)
