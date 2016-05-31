package backend.model.event

import backend.model.location.Location
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface TeamRepository : CrudRepository<Team, Long> {
    fun findById(id: Long): Team?

    @Query("Select p.id from Posting p inner join p.user u inner join u.userRoles r where r.currentTeam.id = :id order by p.id desc")
    fun findPostingsById(@Param("id") id: Long): List<Long>

    @Query("Select l from Location l inner join l.team t where t.id = :id order by l.date asc")
    fun findLocationByTeamId(@Param("id") id: Long): List<Location>

    @Query("Select l from Location l inner join l.team t where t.id = :id order by l.distance desc")
    fun getLocationMaxDistanceById(@Param("id") id: Long): List<Location>

    @Query("Select i from Invitation i where i.invitee.value = :email")
    fun findInvitationsWithEmail(@Param("email") email: String): List<Invitation>

    @Query("Select i from Invitation i where i.invitee.value = :email and i.team.event.id = :eventId")
    fun findInvitationsWithEmailAndEventId(@Param("email") email: String, @Param("eventId") eventId: Long): List<Invitation>

    @Query("Select i from Invitation i where i.invitationToken = :code")
    fun findInvitationsByInviteCode(@Param("code") code: String): Invitation?

    fun findByEventId(eventId: Long): List<Team>

    @Query("from Team t where t.name like concat('%',:search,'%')")
    fun searchByString(@Param("search") search: String): List<Team>
}
