package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.challenges.ChallengeService
import backend.model.event.EventService
import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.misc.EmailAddress
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.CacheNames.POSTINGS
import backend.util.CacheNames.TEAMS
import backend.util.data.DonateSums
import backend.util.getSignedJwtToken
import backend.view.InvitationView
import backend.view.TeamView
import backend.view.posting.PostingView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/event/{eventId}/team")
class TeamController(private val teamService: TeamService,
                     private val eventService: EventService,
                     private val configurationService: ConfigurationService,
                     private val userService: UserService,
                     private val challengeService: ChallengeService) {

    private val JWT_SECRET: String = configurationService.getRequired("org.breakout.api.jwt_secret")
    private val PAGE_SIZE: Int = configurationService.getRequired("org.breakout.api.page_size").toInt()
    private val logger: Logger = LoggerFactory.getLogger(TeamController::class.java)


    /**
     * POST /event/{eventId}/team/leave/
     * The currently authenticated user can leave it's team at this endpoint
     */
    @Caching(evict = arrayOf(CacheEvict(POSTINGS, allEntries = true), CacheEvict(TEAMS, allEntries = true)))
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/leave/")
    fun leaveTeam(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, String> {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class) ?: throw BadRequestException("User is no participant")
        val team = participant.getCurrentTeam() ?: throw BadRequestException("User is no part of a team")
        teamService.leave(team, participant)
        return mapOf("message" to "success")
    }

    /**
     * GET /event/{eventId}/team/invitation/
     * Show all invitations for the currently authenticated user in requested event
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/invitation/")
    fun showInvitationsForUserAndEvent(@PathVariable eventId: Long,
                                       @AuthenticationPrincipal customUserDetails: CustomUserDetails): Iterable<InvitationView> {
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val invitations = teamService.findInvitationsForUserAndEvent(user, eventId)
        return invitations.map(::InvitationView)
    }

    /**
     * POST /event/{id}/team/
     * creates a new Team, with creator as first member
     */
    @CacheEvict(TEAMS, allEntries = true)
    @ResponseStatus(CREATED)
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun createTeam(@PathVariable eventId: Long,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                   @Valid @RequestBody body: TeamView): TeamView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val event = eventService.findById(eventId) ?: throw NotFoundException("No event with id $eventId")
        val creator = user.getRole(Participant::class) ?: throw UnauthorizedException("User is no participant")
        val description = body.description ?: ""
        val name = body.name ?: throw BadRequestException("Team must have a name. Missing in body")
        if (name.isEmpty()) throw BadRequestException("Team name cannot be empty")
        val team = teamService.create(creator, name, description, event)

        team.profilePic.uploadToken = getSignedJwtToken(JWT_SECRET, team.profilePic.id.toString())

        return TeamView(team)
    }

    /**
     * PUT /event/{id}/team/{teamId}/
     * allows teammembers to edit teamname and description
     */
    @Caching(evict = arrayOf(CacheEvict(POSTINGS, allEntries = true), CacheEvict(TEAMS, allEntries = true)))
    @PutMapping("/{teamId}/")
    @PreAuthorize("isAuthenticated()")
    fun editTeam(@PathVariable eventId: Long,
                 @PathVariable teamId: Long,
                 @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                 @Valid @RequestBody body: TeamView): TeamView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")

        checkAuthenticationForEditTeam(team, user)

        body.hasStarted?.let {
            if (user.hasRole(Admin::class)) team.hasStarted = it
            else throw UnauthorizedException("Only an admin can change the hasStarted property of a team")
        }

        team.description = body.description ?: team.description
        team.name = body.name ?: team.name

        teamService.save(team)

        team.profilePic.uploadToken = getSignedJwtToken(JWT_SECRET, team.profilePic.id.toString())

        return TeamView(team)
    }

    private fun checkAuthenticationForEditTeam(team: Team, user: User) {
        val role = user.getRole(Participant::class)
                ?: user.getRole(Admin::class)

        if (role is Participant) {
            if (!team.members.contains(role)) throw UnauthorizedException("User is not part of team")
        }
    }


    /**
     * POST /event/{eventId}/team/{teamId}/invitation/
     * invites a user with given email to existing Team
     */
    @ResponseStatus(CREATED)
    @PostMapping("/{teamId}/invitation/")
    @PreAuthorize("isAuthenticated()")
    fun inviteUser(@PathVariable eventId: Long,
                   @PathVariable teamId: Long,
                   @Valid @RequestBody body: Map<String, Any>): Map<String, String> {

        if (!eventService.exists(eventId)) throw NotFoundException("No event with id $eventId")

        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")
        val emailString = body["email"] as? String ?: throw BadRequestException("body is missing field email")
        val email = EmailAddress(emailString)
        teamService.invite(email, team)

        return mapOf("status" to "sent invitation")
    }

    /**
     * POST /event/{eventId}/team/{teamId}/member/
     * allows user with Invitation to join Team
     */
    @Caching(evict = arrayOf(CacheEvict(POSTINGS, allEntries = true), CacheEvict(TEAMS, allEntries = true)))
    @ResponseStatus(CREATED)
    @PostMapping("/{teamId}/member/")
    @PreAuthorize("isAuthenticated()")
    fun joinTeam(@PathVariable eventId: Long,
                 @PathVariable teamId: Long,
                 @AuthenticationPrincipal customUserDetails: CustomUserDetails,
                 @Valid @RequestBody body: Map<String, String>): TeamView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (!eventService.exists(eventId)) throw NotFoundException("No event with id $eventId")

        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")
        val emailString = body["email"] ?: throw BadRequestException("body is missing field email")
        val email = EmailAddress(emailString)

        if (user.email != email.toString()) throw BadRequestException("Authorized user and email from request body don't match")
        val participant = user.getRole(Participant::class) ?: throw RuntimeException("User is no participant")

        teamService.join(participant, team)

        return TeamView(team)
    }

    /**
     * GET /event/{eventId}/team/{teamId}/
     * gets a specific Team
     */
    @GetMapping("/{teamId}/")
    fun showTeam(@PathVariable teamId: Long): TeamView {
        val team = teamService.findOne(teamId) ?: throw NotFoundException("team with id $teamId does not exist")
        val teamDonateSum = teamService.getDonateSum(teamId)
        val teamDistance = teamService.getDistance(teamId)
        return TeamView(team, teamDistance, teamDonateSum)
    }

    /**
     * GET /event/{eventId}/team/
     * gets all Teams for Event
     */
    @Cacheable(TEAMS)
    @GetMapping("/")
    fun showTeamsByEvent(@PathVariable eventId: Long): Iterable<TeamView> {
        val teams = teamService.findByEventId(eventId)
        return teams.map(::TeamView)
    }

    /**
     * GET /event/{eventId}/team/{teamId}/posting/
     * gets all Postings for Team
     */
    @GetMapping("/{teamId}/posting/")
    fun getTeamPostingIds(@PathVariable teamId: Long,
                          @RequestParam(value = "page", required = false) page: Int?,
                          @RequestParam(value = "userid", required = false) userId: Long?): List<PostingView> {
        return teamService.findPostingsById(teamId, page ?: 0, PAGE_SIZE).map {
            PostingView(it.hasLikesBy(userId), it.challenge?.let {
                challengeService.findOne(it)
            })
        }
    }

    /**
     * GET /event/{eventId}/team/{id}/distance/
     * Get the actual distance and the linear distance for a team
     *
     * Example: Having a route of a team from A -> B -> C
     * Actual distance = |A -> B| + |B -> C|
     * Linear distance = |A -> C|
     */
    @GetMapping("/{id}/distance/")
    fun getTeamDistance(@PathVariable("id") teamId: Long): Map<String, Double> {
        return mapOf("distance" to teamService.getDistance(teamId))
    }

    /**
     * GET /event/{eventId}/team/{id}/donatesum/
     * Get the sponsored sums per team
     */
    @GetMapping("/{id}/donatesum/")
    fun getTeamDonateSum(@PathVariable("id") teamId: Long): DonateSums {
        return teamService.getDonateSum(teamId)
    }
}
