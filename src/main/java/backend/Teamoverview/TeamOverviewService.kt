package backend.Teamoverview

import org.springframework.stereotype.Service

interface TeamOverviewService {
    fun findAll(): Iterable<TeamOverview>
    fun findByTeamId(teamId: Long): TeamOverview?
    fun addComment(teamId: Long, comment: String)
}

