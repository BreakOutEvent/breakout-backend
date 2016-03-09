package backend.model.location

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.user.Participant
import com.grum.geocalc.Point

class Location : BasicEntity {

    lateinit var point: Point
        private set

    lateinit var uploader: Participant
        private set

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
