package backend.Teamoverview

interface TeamOverviewService {
    fun findAll(): Iterable<TeamOverview>
    fun findByTeamId(teamId: Long): TeamOverview?
    fun addComment(teamId: Long, comment: String)
}

