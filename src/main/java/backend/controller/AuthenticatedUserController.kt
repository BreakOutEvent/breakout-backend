package backend.controller

import backend.configuration.CustomUserDetails
import backend.model.event.TeamService
import backend.model.user.UserService
import backend.view.InvitationView
import backend.view.UserView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class AuthenticatedUserController {


    private val userService: UserService
    private val teamService: TeamService

    @Autowired
    constructor(userService: UserService, teamService: TeamService) {
        this.userService = userService
        this.teamService = teamService
    }

    @RequestMapping("/", method = arrayOf(GET))
    fun getAuthenticatedUser(@AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        return UserView(user)
    }

    /**
     * GET /me/invitation/
     * Show all invitations for the currently authenticated user
     */
    @RequestMapping("/invitation/")
    fun showInvitationsForUser(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Iterable<InvitationView> {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val invitations = teamService.findInvitationsForUser(user)
        return invitations.map { InvitationView(it) }
    }
}
