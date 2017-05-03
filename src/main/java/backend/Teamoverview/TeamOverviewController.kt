package backend.Teamoverview

import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@RestController
@RequestMapping("/teamoverview/")
class TeamOverviewController(val teamOverviewService: TeamOverviewService) {

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    fun getTeamOverview(): Iterable<TeamOverview> {
        return teamOverviewService.findAll()
    }

    @PostMapping("{teamId}/lastContactWithHeadquarters/")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(CREATED)
    fun addContactCommentForTeam(@PathVariable teamId: Long,
                                 @Valid @RequestBody body: ContactCommentBody) {
        teamOverviewService.addComment(teamId, body.comment)
    }

    class ContactCommentBody {
        @NotNull
        @Size(min = 1)
        lateinit var comment: String
    }
}

