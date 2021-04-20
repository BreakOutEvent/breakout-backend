package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.challenges.ChallengeService
import backend.model.event.EventService
import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.removeBlockedBy
import backend.model.removeReported
import backend.model.user.*
import backend.services.ConfigurationService
import backend.util.CacheNames.LOCATIONS
import backend.util.CacheNames.POSTINGS
import backend.util.CacheNames.TEAMS
import backend.util.data.DonateSums
import backend.view.InvitationView
import backend.view.TeamAndTeamEntryFeeInvoiceView
import backend.view.TeamView
import backend.view.posting.PostingView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
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
     * POST /event/{eventId}/team/sendDonationPromise/
     */
    @PostMapping("/sendDonationPromise")
    fun sendGeneratedDonationPromiseToTeams(@PathVariable eventId: Long): ResponseEntity<Any> {
        val event = eventService.findById(eventId) ?: throw NotFoundException("Event with id $eventId does not exist")
        teamService.sendEmailsToTeamsWithDonationOverview(event)
        return ResponseEntity.ok(mapOf("message" to "ok"))
    }

    /**
     * POST /event/{eventId}/team/leave/
     * The currently authenticated user can leave it's team at this endpoint
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(TEAMS, allEntries = true))])
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
    @Caching(evict = [(CacheEvict(TEAMS, allEntries = true)), (CacheEvict(LOCATIONS, allEntries = true))])
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
        val postaddress = body.postaddress
        val team = teamService.create(creator, name, description, event, body.profilePic?.let(::Media), postaddress)


        return TeamView(team, customUserDetails.id)
    }

    /**
     * PUT /event/{id}/team/{teamId}/
     * allows teammembers to edit teamname and description
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(LOCATIONS, allEntries = true)), (CacheEvict(TEAMS, allEntries = true))])
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
            if (user.hasAuthority(EventManager::class   )) team.hasStarted = it
            else throw UnauthorizedException("Only an admin can change the hasStarted property of a team")
        }

        team.description = body.description ?: team.description
        team.name = body.name ?: team.name
        team.profilePic = body.profilePic?.let(::Media) ?: team.profilePic
        team.asleep = body.asleep ?: team.asleep
        team.postaddress = body.postaddress ?: team.postaddress

        teamService.save(team)

        return TeamView(team, customUserDetails.id)
    }

    private fun checkAuthenticationForEditTeam(team: Team, user: User) {


        val userIsAdmin = user.hasAuthority(EventManager::class)
        val userIsTeamMember = user
                .getRole(Participant::class)
                ?.let { team.members.contains(it) }
                ?: false

        if (!(userIsAdmin || userIsTeamMember)) {
            throw UnauthorizedException("User is neither admin nor part of the team")
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
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(TEAMS, allEntries = true))])
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

        return TeamView(team, customUserDetails.id)
    }

    /**
     * GET /event/{eventId}/team/{teamId}/
     * gets a specific Team
     */
    @GetMapping("/{teamId}/")
    fun showTeam(@PathVariable teamId: Long,
                 @AuthenticationPrincipal customUserDetails: CustomUserDetails?): TeamView {
        val team = teamService.findOne(teamId) ?: throw NotFoundException("team with id $teamId does not exist")

        if (team.isBlockedBy(customUserDetails?.id))
            throw NotFoundException("All members of team with id $teamId were blocked")

        val teamDonateSum = teamService.getDonateSum(teamId)
        val teamDistance = teamService.getDistance(teamId)
        val score = teamService.getScore(team)
        return TeamView(team, teamDistance, teamDonateSum, score, customUserDetails?.id)
    }

    /**
     * GET /event/{eventId}/team/
     * gets all Teams for Event
     */
    @Cacheable(TEAMS, sync = true)
    @GetMapping("/")
    fun showTeamsByEvent(@PathVariable eventId: Long,
                         @AuthenticationPrincipal customUserDetails: CustomUserDetails?): Iterable<TeamView> {

        logger.info("Cache miss on /event/$eventId/team/")
        val teams = teamService.findByEventId(eventId)
        return teams.removeBlockedBy(customUserDetails?.id).map { TeamView(it, customUserDetails?.id) }
    }

    /**
     * GET /event/{eventId}/team/{teamId}/posting/
     * gets all Postings for Team
     */
    @GetMapping("/{teamId}/posting/")
    fun getTeamPostingIds(@PathVariable teamId: Long,
                          @RequestParam(value = "page", required = false) page: Int?,
                          @AuthenticationPrincipal customUserDetails: CustomUserDetails?): List<PostingView> {
        // TODO: Remove hardcoded page size of 150
        return teamService.findPostingsById(teamId, page ?: 0, 150).removeReported().removeBlockedBy(customUserDetails?.id).map {
            PostingView(it.hasLikesBy(customUserDetails?.id), it.challenge?.let {
                challengeService.findOne(it)
            }, customUserDetails?.id)
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


    @PreAuthorize("hasAuthority('FINANCE_MANAGER')")
    @GetMapping("/teamfee/")
    fun getTeamFees(@PathVariable eventId: Long): Iterable<TeamAndTeamEntryFeeInvoiceView> {
        return teamService.findByEventId(eventId).map(::TeamAndTeamEntryFeeInvoiceView)
    }
}
