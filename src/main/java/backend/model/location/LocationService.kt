package backend.model.location

import backend.model.misc.Coord
import backend.model.user.Participant
import java.time.LocalDateTime

interface LocationService {

    fun create(coord: Coord, participant: Participant, date: LocalDateTime, doGeoCode: Boolean = false): Location

    fun save(location: Location): Location

    fun findAll(): Iterable<Location>

    fun findByTeamId(id: Long): Iterable<Location>

    fun findByEventId(id: Long): Iterable<Location>

    fun findByEventIdSinceId(eventId: Long, sinceId: Long): Iterable<Location>

    fun findByTeamIdSince(teamId: Long, sinceId: Long): Iterable<Location>
}
