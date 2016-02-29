package backend.model.event

import backend.model.posting.Posting
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EventRepository : CrudRepository<Event, Long> {
    fun findByCity(city: String): Iterable<Event>
    fun findById(id: Long): Event?

    @Query("Select p.id from Posting p inner join p.user u inner join u.userRoles r inner join r.currentTeam t where t.event.id = :id order by p.date asc")
    fun findPostingsById(@Param("id") id: Long): List<Long>

    @Query("Select p from Posting p inner join p.user u inner join u.userRoles r inner join r.currentTeam t where t.event.id = :id and latitude is not null and longitude is not null order by p.date asc")
    fun findLocationPostingsById(@Param("id") id: Long): List<Posting>
}
