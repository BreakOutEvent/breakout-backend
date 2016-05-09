package backend.model.sponsoring

import org.springframework.data.repository.CrudRepository

interface SponsoringRepository : CrudRepository<Sponsoring, Long> {
    fun findByTeamId(teamId: Long): Iterable<Sponsoring>
    fun findBySponsorCoreId(teamId: Long): Iterable<Sponsoring>
}

