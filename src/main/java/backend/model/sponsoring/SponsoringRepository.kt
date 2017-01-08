package backend.model.sponsoring

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SponsoringRepository : CrudRepository<Sponsoring, Long> {
    fun findByTeamId(teamId: Long): Iterable<Sponsoring>

    @Query("Select s from Sponsoring s where s.registeredSponsor.account.id = :id")
    fun findBySponsorAccountId(@Param("id") teamId: Long): Iterable<Sponsoring>
}

