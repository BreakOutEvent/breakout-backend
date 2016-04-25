package backend.model.event

import backend.model.location.Location
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EventRepository : CrudRepository<Event, Long> {

    fun findById(id: Long): Event?

    @Query("Select p.id from Posting p inner join p.user u inner join u.userRoles r inner join r.currentTeam t where t.event.id = :id order by p.date asc")
    fun findPostingsById(@Param("id") id: Long): List<Long>

    @Query("Select l from Location l inner join l.team t where t.event.id = :id order by l.date asc")
    fun findLocationPostingsById(@Param("id") id: Long): List<Location>

    //TODO: write correct query
    @Query("Select l from Location l inner join l.team t where t.event.id = :id order by l.distance asc")
    fun getLocationMaxDistanceByIdEachTeam(@Param("id") id: Long): List<Location>

}
