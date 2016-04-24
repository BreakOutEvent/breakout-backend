package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.EventService
import backend.model.event.TeamService
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.getSignedJwtToken
import backend.view.InvitationView
import backend.view.TeamView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

@RestController
@RequestMapping("/event/{eventId}/team")
open class TeamController {

    private val teamService: TeamService
    private val eventService: EventService
    private val JWT_SECRET: String
    private val configurationService: ConfigurationService
    private val userService: UserService

    @Autowired
    constructor(teamService: TeamService,
                eventService: EventService,
                configurationService: ConfigurationService,
                userService: UserService) {

        this.teamService = teamService
        this.eventService = eventService
        this.configurationService = configurationService
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        this.userService = userService
    }


    /**
     * POST /event/{id}/team/leave/
     * The currently authenticated user can leave it's team at this endpoint
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/leave/", method = arrayOf(POST))
    open fun leaveTeam(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, String> {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw BadRequestException("User is no participant")
        val team = participant.currentTeam ?: throw BadRequestException("User is no part of a team")
        teamService.leave(team, participant)
        return mapOf("message" to "success")
    }

    /**
     * GET /event/{id}/team/invitation/
     * Show all invitations for the currently authenticated user
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/invitation/")
    open fun showInvitationsForUser(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Iterable<InvitationView> {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val invitations = teamService.findInvitationsForUser(user)
        return invitations.map { InvitationView(it) }
    }

    /**
     * POST /event/{id}/team/
     * creates a new Team, with creator as first member
     */
    @ResponseStatus(CREATED)
    @RequestMapping("/", method = arrayOf(POST))
    @PreAuthorize("isAuthenticated()")
    open fun createTeam(@PathVariable eventId: Long,
                        @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                        @Valid @RequestBody body: TeamView): TeamView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val event = eventService.findById(eventId) ?: throw NotFoundException("No event with id $eventId")
        val creator = user.getRole(Participant::class) ?: throw UnauthorizedException("User is no participant")
        var team = teamService.create(creator, body.name!!, body.description!!, event)

        team.profilePic.uploadToken = getSignedJwtToken(JWT_SECRET, team.profilePic.id.toString())

        return TeamView(team)
    }

    /**
     * POST /event/{id}/team/{id}/invitation/
     * invites a user with given email to existing Team
     */
    @ResponseStatus(CREATED)
    @RequestMapping("/{teamId}/invitation/", method = arrayOf(POST))
    @PreAuthorize("isAuthenticated()")
    open fun inviteUser(@PathVariable eventId: Long,
                        @PathVariable teamId: Long,
                        @Valid @RequestBody body: Map<String, Any>): Map<String, String> {

        if (eventService.exists(eventId) == false) throw NotFoundException("No event with id $eventId")

        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")
        val emailString = body["email"] as? String ?: throw BadRequestException("body is missing field email")
        val email = EmailAddress(emailString)
        teamService.invite(email, team)

        return mapOf("status" to "sent invitation")
    }

    /**
     * POST /event/{id}/team/{id}/member/
     * allows user with Invitation to join Team
     */
    @ResponseStatus(CREATED)
    @RequestMapping("/{teamId}/member/", method = arrayOf(POST))
    @PreAuthorize("isAuthenticated()")
    open fun joinTeam(@PathVariable eventId: Long,
                      @PathVariable teamId: Long,
                      @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                      @Valid @RequestBody body: Map<String, String>): TeamView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (eventService.exists(eventId) == false) throw NotFoundException("No event with id $eventId")

        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")
        val emailString = body["email"] ?: throw BadRequestException("body is missing field email")
        val email = EmailAddress(emailString)

        if (user.email != email.toString()) throw BadRequestException("Authorized user and email from request body don't match")
        val participant = user.getRole(Participant::class) ?: throw RuntimeException("User is no participant")

        //TODO: Move join to service Layer, instead of model
        team.join(participant)
        teamService.save(team)

        return TeamView(team)
    }

    @RequestMapping("/{id}/")
    open fun showTeam(@PathVariable id: Long): TeamView {
        val team = teamService.findOne(id) ?: throw NotFoundException("team with id $id does not exist")
        return TeamView(team)
    }

    @RequestMapping("/{id}/posting/")
    open fun getTeamPostingIds(@PathVariable id: Long): List<Long> {
        val postingIds = teamService.findPostingsById(id) ?: throw NotFoundException("team with id $id does not exist")
        return postingIds
    }

    /**
     * GET /event/{eventId}/team/{id}/distance/
     * Get the actual distance and the linear distance for a team
     * TODO: Add endpoint which supports the actual and linear from locations
     * TODO: This endpoint only considers postings with locations, but not locations without postings
     *
     * Example: Having a route of a team from A -> B -> C
     * Actual distance = |A -> B| + |B -> C|
     * Linear distance = |A -> C|
     */
    @RequestMapping("/{id}/distance/")
    open fun getTeamDistance(@PathVariable("id") teamId: Long): Map<String, Any> {

        val linearDistance = this.teamService.getLinearDistanceForTeamFromPostings(teamId)
        val actualDistance = this.teamService.getActualDistanceForTeamFromPostings(teamId)

        return mapOf("actualdistance" to actualDistance, "distance" to linearDistance)
    }
}
