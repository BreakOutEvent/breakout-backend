package backend.Teamoverview

import org.springframework.data.projection.ProjectionFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/teamoverview/")
class TeamOverviewController(val teamOverviewService: TeamOverviewService) {

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    fun getTeamOverview(): Iterable<TeamOverview> {
        return teamOverviewService.findAll()
    }
}

