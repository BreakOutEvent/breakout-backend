package backend.model.event

import backend.model.posting.Posting
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface TeamRepository : CrudRepository<Team, Long> {
    fun findById(id: Long): Team

    @Query("Select p.id from Posting p inner join p.user u inner join u.userRoles r where r.currentTeam.id = :id order by p.date asc")
    fun findPostingsById(@Param("id") id: Long): List<Long>

    @Query("Select p from Posting p inner join p.user u inner join u.userRoles r where r.currentTeam.id = :id and p.location is not null order by p.date asc")
    fun findLocationPostingsById(@Param("id") id: Long): List<Posting>

    @Query("Select p from Posting p inner join p.user u inner join u.userRoles r where r.currentTeam.id = :id and distance is not null order by p.distance asc")
    fun getPostingMaxDistanceById(@Param("id") id: Long): List<Posting>

    @Query("Select i from Invitation i where i.invitee.value = :email")
    fun findInvitationsWithEmail(@Param("email") email: String): List<Invitation>

    @Query("Select i from Invitation i where i.invitee.value = :email and i.team.event.id = :eventId")
    fun findInvitationsWithEmailAndEventId(@Param("email") email: String, @Param("eventId") eventId: Long): List<Invitation>

    @Query("Select i from Invitation i where i.invitationToken = :code")
    fun findInvitationsByInviteCode(@Param("code") code: String): Invitation?
}
