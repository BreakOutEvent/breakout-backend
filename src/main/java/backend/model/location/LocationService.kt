package backend.model.location

import backend.model.event.Team
import backend.model.misc.Coord
import backend.model.user.Participant
import java.time.LocalDateTime

interface LocationService {

    fun create(coord: Coord, participant: Participant, date: LocalDateTime, doGeoCode: Boolean = false): Location

    fun adminCreate(coord: Coord, participant: Participant, date: LocalDateTime, doGeoCode: Boolean): Location

    fun save(location: Location): Location

    fun findAll(): Iterable<Location>

    fun findByTeamId(id: Long, perTeam: Int): Iterable<Location>

    fun findByEventId(id: Long, perTeam: Int): Map<Team, Iterable<Location>>

    fun generateSpeed()

}
