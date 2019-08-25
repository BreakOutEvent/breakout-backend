package backend.teamoverview

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
    @PreAuthorize("hasAuthority('EVENT_MANAGER')")
    fun getTeamOverview(): Iterable<TeamOverview> {
        return teamOverviewService.findAll()
    }

    @GetMapping("{teamId}/calls/")
    @PreAuthorize("hasAuthority('EVENT_MANAGER')")
    fun getCallsForTeam(@PathVariable teamId: Long): Iterable<TeamOverview.Contact> {
        return teamOverviewService.allCalls(teamId)
    }

    @PostMapping("{teamId}/lastContactWithHeadquarters/")
    @PreAuthorize("hasAuthority('EVENT_MANAGER')")
    @ResponseStatus(CREATED)
    fun addContactCommentForTeam(@PathVariable teamId: Long,
                                 @Valid @RequestBody body: ContactCommentBody,
                                 @AuthenticationPrincipal customUserDetails: CustomUserDetails) {

        val admin = userService.getUserFromCustomUserDetails(customUserDetails)
        teamOverviewService.addComment(teamId, body.reason, body.comment, admin.account)
    }

    class ContactCommentBody {
        @NotNull
        lateinit var reason: ContactWithHeadquarters.Reason
        var comment: String? = null
    }
}

