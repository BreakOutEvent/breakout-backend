package backend.model.location

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface LocationRepository : JpaRepository<Location, Long> {

    @Query("SELECT * FROM location WHERE id IN (SELECT id FROM (SELECT id, @rownum \\:= @rownum + 1 AS number FROM location JOIN (SELECT @rownum \\:= 0) R WHERE team_id = ?1) a WHERE a.id = ?2 OR a.id = ?3 OR a.number mod (ceil(?4/?5)) = 0 ORDER BY a.id DESC)", nativeQuery = true)
    fun findByTeamId(id: Long, maxId: Long, minId: Long, modSelector: Long, perTeam: Int): Iterable<Location>

    @Query("Select floor(max(a.id)) as maxId, floor(min(a.id)) as minId, floor(max(a.number)) as modSelector from (Select id, @rownum \\:= @rownum + 1 as number from location join (Select @rownum \\:= 0) r where is_during_event and team_id = ?1) a", nativeQuery = true)
    fun findTeamLocationBounds(id: Long): List<Any>

    fun findBySpeedToLocationAndIsDuringEvent(speedToLocation: Double?, isDuringEvent: Boolean): List<Location>

    @Query("Select l from Location l WHERE l.team.id = :id AND l.date <= :date AND l.id != :locationId order by l.date desc")
    fun findByTeamIdAndPriorOrderByDateDesc(@Param("id") id: Long?, @Param("locationId") locationId: Long?, @Param("date") date: LocalDateTime): List<Location>
}
