package backend.model.location

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface LocationRepository : CrudRepository<Location, Long> {

    @Query("Select l from Location l where l.team.id = :id")
    fun findByTeamId(@Param("id") id: Long): Iterable<Location>

}
