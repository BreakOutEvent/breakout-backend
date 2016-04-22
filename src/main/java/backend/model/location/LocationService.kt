package backend.model.location

import backend.model.user.Participant
import java.time.LocalDateTime

interface LocationService {
    fun create(point: Point, particpant: Participant, date: LocalDateTime): Location
    fun save(location: Location): Location
    fun findAll(): Iterable<Location>
    fun findByTeamId(id: Long): Iterable<Location>
}
