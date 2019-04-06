package backend.teamoverview

import backend.model.user.UserAccount

interface TeamOverviewService {
    fun findAll(): Iterable<TeamOverview>
    fun allCalls(teamId: Long): Iterable<ContactWithHeadquarters>
    fun addComment(teamId: Long,
                   reason: ContactWithHeadquarters.Reason,
                   comment: String?,
                   admin: UserAccount)
}

