package backend.model.location

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface LocationRepository : CrudRepository<Location, Long> {

    @Query("Select l from Location l where l.team.id = :id")
    fun findByTeamId(@Param("id") id: Long): Iterable<Location>

    @Query("Select l from Location l where  l.team.event.id = :id")
    fun findByEventId(@Param("id") id: Long): Iterable<Location>

    @Query("Select l from Location l where  l.team.event.id = :id and l.id > :sinceId order by l.id desc")
    fun findByEventIdSinceId(@Param("id") id: Long, @Param("sinceId") sinceId: Long): Iterable<Location>

    @Query("Select l from Location l where l.team.id = :id and l.id > :sinceId order by l.id desc")
    fun findByTeamIdSinceId(@Param("id") id: Long, @Param("sinceId") sinceId: Long): Iterable<Location>

}
