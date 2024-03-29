package backend.model.event

import backend.model.location.Location
import backend.model.user.Participant
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EventRepository : CrudRepository<Event, Long> {

    fun findById(id: Long): Event?

    @Query("Select p.id from Posting p inner join p.user u inner join u.userRoles r inner join r.currentTeam t where t.event.id = :id order by p.date asc")
    fun findPostingsById(@Param("id") id: Long): List<Long>

    @Query("Select l from Location l inner join l.team t where t.event.id = :id order by l.date asc")
    fun findLocationPostingsById(@Param("id") id: Long): List<Location>


    @Query("SELECT loc FROM Location loc WHERE (loc.distance, loc.team.id) IN (Select max(l.distance), l.team.id from Location l inner join l.team t where t.event.id = :id group by l.team.id)")
    fun getLocationMaxDistanceByIdEachTeam(@Param("id") id: Long): List<Location>
    
    @Query("select t.members from Team t where t.event.id = :eventId ")
    fun listTeamMembersOfEvent(@Param("eventId") EventId: Long): List<Participant>
}

interface WhitelistEmailRepository : CrudRepository<WhitelistEmailEntry, Long> {
    @Query("Select e from WhitelistEmailEntry e where e.invitee.value = :email")
    fun findWhitelistEmailEntriesByEmail(@Param("email") email: String): List<WhitelistEmailEntry>
}

interface WhitelistDomainRepository : CrudRepository<WhitelistDomainEntry, Long> {
    @Query("Select e from WhitelistDomainEntry e where e.domain = :domain")
    fun findWhitelistDomainsEntriesByDomain(@Param("domain") domain: String): List<WhitelistDomainEntry>
}
