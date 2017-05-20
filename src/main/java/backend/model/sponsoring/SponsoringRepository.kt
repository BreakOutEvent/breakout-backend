package backend.model.sponsoring

import backend.model.user.Sponsor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SponsoringRepository : CrudRepository<Sponsoring, Long> {
    fun findByTeamId(teamId: Long): Iterable<Sponsoring>

    @Query("Select s from Sponsoring s where s.registeredSponsor.account.id = :id")
    fun findBySponsorAccountId(@Param("id") teamId: Long): Iterable<Sponsoring>

    @Query("select s from Sponsoring sp join sp.registeredSponsor as s where sp.team.event.id = :eventId")
    fun findAllRegisteredSponsorsWithSponsoringsAtEvent(@Param("eventId") eventId: Long): Iterable<Sponsor>

    @Query("select s from Sponsoring sp join sp.unregisteredSponsor as s where sp.team.event.id = :eventId")
    fun findAllUnregisteredSponsorsWithSponsoringsAtEvent(@Param("eventId") eventId: Long): Iterable<UnregisteredSponsor>
}

