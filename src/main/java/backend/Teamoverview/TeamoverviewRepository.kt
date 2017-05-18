package backend.Teamoverview

import org.springframework.data.repository.CrudRepository

interface TeamoverviewRepository: CrudRepository<TeamOverview, Long> {
    fun findByTeamId(teamId: Long): TeamOverview?
}

