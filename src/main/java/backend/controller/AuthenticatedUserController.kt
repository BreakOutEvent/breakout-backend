package backend.controller

import backend.configuration.CustomUserDetails
import backend.model.event.EventService
import backend.model.event.TeamService
import backend.model.removeBlocking
import backend.model.user.UserService
import backend.view.EventView
import backend.view.InvitationView
import backend.view.user.BasicUserView
import backend.view.user.UserView
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class AuthenticatedUserController(
        private val userService: UserService,
        private val teamService: TeamService,
        private val eventService: EventService
) {

    /**
     * GET /me/
     * Get information to the currently authenticated user
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    fun getAuthenticatedUser(@AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        return UserView(user)
    }

    /**
     * GET /me/
     * Get information to the currently authenticated user
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/blocked/")
    fun getBlockedUsers(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Iterable<BasicUserView> {

        return userService.getAllUsersBlockedBy(customUserDetails.id).map(::BasicUserView)
    }


    /**
     * GET /me/invitation/
     * Show all invitations for the currently authenticated user
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/invitation/")
    fun showInvitationsForUser(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Iterable<InvitationView> {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val invitations = teamService.findInvitationsForUser(user)
        return invitations.map(::InvitationView)
    }

    /**
     * GET /me/event/open/
     * Show events where the user is allowed to register
     */
    @GetMapping("/event/open/")
    fun getEventsOpenForRegistration(@AuthenticationPrincipal customUserDetails: CustomUserDetails?): Iterable<EventView> {
        val user = customUserDetails?.let { userService.getUserFromCustomUserDetails(it) }
        return eventService.findEvensOpenForRegistration(user).map(::EventView)
    }
}
