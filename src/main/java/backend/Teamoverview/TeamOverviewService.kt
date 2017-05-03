package backend.Teamoverview

import org.springframework.stereotype.Service

interface TeamOverviewService {
    fun findAll(): Iterable<TeamOverview>
}

