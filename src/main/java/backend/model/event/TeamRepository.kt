package backend.model.event

import backend.model.location.Location
import backend.model.posting.Posting
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface TeamRepository : CrudRepository<Team, Long> {
    fun findById(id: Long): Team?

    @Query("Select p from Posting p inner join p.user u inner join u.userRoles r where r.currentTeam.id = :teamId order by p.id desc")
    fun findPostingsByTeamId(@Param("teamId") id: Long, pageable: Pageable): List<Posting>

    @Query("Select l from Location l inner join l.team t where t.id = :id order by l.date asc")
    fun findLocationByTeamId(@Param("id") id: Long): List<Location>

    @Query("Select i from Invitation i where i.invitee.value = :email")
    fun findInvitationsWithEmail(@Param("email") email: String): List<Invitation>

    @Query("Select i from Invitation i where i.invitee.value = :email and i.team.event.id = :eventId")
    fun findInvitationsWithEmailAndEventId(@Param("email") email: String, @Param("eventId") eventId: Long): List<Invitation>

    @Query("Select i from Invitation i where i.invitationToken = :code")
    fun findInvitationsByInviteCode(@Param("code") code: String): Invitation?

    fun findByEventId(eventId: Long): List<Team>

    @Query("from Team t where t.name like concat('%',:search,'%')")
    fun searchByString(@Param("search") search: String): List<Team>

    //language=HQL
    @Query("Select new backend.model.event.TeamSummaryProjection(t.name, t.id, t.event.id, t.event.title) from Team t")
    fun findAllTeamSummaryProjections(): Iterable<TeamSummaryProjection>
}
