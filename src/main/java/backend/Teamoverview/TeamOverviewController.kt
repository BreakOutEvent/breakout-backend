package backend.Teamoverview

import backend.configuration.CustomUserDetails
import backend.model.user.UserService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@RestController
@RequestMapping("/teamoverview/")
class TeamOverviewController(val teamOverviewService: TeamOverviewService,
                             val userService: UserService) {

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    fun getTeamOverview(): Iterable<TeamOverview> {
        return teamOverviewService.findAll()
    }

    @GetMapping("{teamId}/calls/")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun getCallsForTeam(@PathVariable teamId: Long): Iterable<ContactWithHeadquarters> {
        return teamOverviewService.allCalls(teamId)
    }

    @PostMapping("{teamId}/lastContactWithHeadquarters/")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(CREATED)
    fun addContactCommentForTeam(@PathVariable teamId: Long,
                                 @Valid @RequestBody body: ContactCommentBody,
                                 @AuthenticationPrincipal customUserDetails: CustomUserDetails) {

        val admin = userService.getUserFromCustomUserDetails(customUserDetails)
        teamOverviewService.addComment(teamId, body.comment, admin.account)
    }

    class ContactCommentBody {
        @NotNull
        @Size(min = 1)
        lateinit var comment: String
    }
}

