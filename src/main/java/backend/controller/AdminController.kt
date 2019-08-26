package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.model.challenges.ChallengeService
import backend.model.event.EventService
import backend.model.event.TeamService
import backend.model.location.LocationService
import backend.model.misc.Coord
import backend.model.posting.PostingService
import backend.model.sponsoring.SponsoringService
import backend.model.user.*
import backend.services.mail.MailService
import backend.view.challenge.ChallengeStatusView
import backend.view.challenge.ChallengeView
import backend.view.posting.PostingView
import backend.view.user.AdminTeamLocationView
import backend.view.user.UserView
import com.sun.javaws.exceptions.InvalidArgumentException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.persistence.DiscriminatorValue
import javax.validation.Valid
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@RestController
@RequestMapping("/admin")
class AdminController(private val userService: UserService,
                      private val mailService: MailService,
                      private val teamService: TeamService,
                      private val eventService: EventService,
                      private val challengeService: ChallengeService,
                      private val sponsoringService: SponsoringService,
                      private val locationService: LocationService,
                      private val postingService: PostingService) {


    private val logger: Logger = LoggerFactory.getLogger(AdminController::class.java)

    /**
     * GET /admin/resendmail/
     * Allows Admin to resend failed mails
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/resendmail/")
    fun resendMail(): Map<String, Int> {
        val count = mailService.resendFailed()

        logger.info("Resent $count mails from admin request")
        return mapOf("count" to count)
    }

    /**
     * POST /admin/email/{identifier}/send/
     * Send emails for specific identifiers
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/email/{identifier}/send/")
    fun sendEmail(@PathVariable identifier: String): Map<String, String> {
        when (identifier) {
            "SPONSOR_EVENT_STARTED" -> sponsoringService.sendEmailsToSponsorsWhenEventHasStarted()
            "SPONSOR_EVENT_ENDED" -> sponsoringService.sendEmailsToSponsorsWhenEventHasEnded()
            "TEAM_EVENT_ENDED" -> teamService.sendEmailsToTeamsWhenEventHasEnded()
            else -> throw NotFoundException("identifier $identifier not registered as email trigger")
        }

        return mapOf("message" to "success")
    }

    /**
     * POST /admin/postteamlocation/
     * Generate a team-posting to set a team-location
     */
    @PreAuthorize("hasAuthority('EVENT_MANAGER')")
    @PostMapping("/postteamlocation/")
    fun postTeamLocation(@Valid @RequestBody body: AdminTeamLocationView): PostingView {
        val team = teamService.findOne(body.teamId) ?: throw NotFoundException("Team with ID ${body.teamId} not found")

        val posting = postingService.adminCreatePosting(
                team.members.elementAt(0),
                "Current Location: ${body.city}",
                null,
                Coord(body.latitude, body.longitude),
                LocalDateTime.now())

        return PostingView(posting, null, null)
    }

    /**
     * GET /admin/allchallenges/
     * Allows Admin to get all challenges for current events
     */
    @PreAuthorize("hasAuthority('EVENT_MANAGER')")
    @GetMapping("/allchallenges/")
    fun getAllChallenges(): Iterable<ChallengeView> {
        val currentEvents = eventService.findAll().filter { it.isCurrent }
        return challengeService.findAllChallengesForEvents(currentEvents).map(::ChallengeView)
    }

    /**
     * POST /challenge/{challengeId}/proof/
     * Add proof to a posting
     */
    @PreAuthorize("hasAuthority('EVENT_MANAGER')")
    @PostMapping("/challenge/{challengeId}/proof/")
    fun addProofToChallenge(@PathVariable challengeId: Long,
                            @Valid @RequestBody body: ChallengeStatusView): ChallengeView {

        val challenge = challengeService.findOne(challengeId)
                ?: throw NotFoundException("No challenge with id $challengeId found")

        return when (body.status!!.toLowerCase()) {
            "with_proof" -> {
                val proof = postingService.getByID(body.postingId!!)
                        ?: throw NotFoundException("No posting with id ${body.postingId} found")
                challengeService.addProofAsAdmin(challenge, proof)
            }
            else -> throw BadRequestException("Unknown status for challenge ${body.status}")
        }.let(::ChallengeView)
    }

    /**
     * POST /admin/{id}/makeadmin
     * Turn a user into an Admin
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("user/{id}/admin/")
    fun addAdminRights(@PathVariable id: Long,
                       @RequestParam authority: String): UserView {

        val user = userService.getUserById(id) ?: throw NotFoundException("User with ID $id not found")

        fun <T : UserRole> authority(clazz: KClass<T>): String {
            return clazz.findAnnotation<DiscriminatorValue>()?.value ?: throw NotFoundException("Role corresponding to right was not found")
        }

        when (authority) {
            authority(Admin::class) -> user.addRole(Admin::class)
            authority(EventManager::class) -> user.addRole(EventManager::class)
            authority(EventOwner::class) -> user.addRole(EventOwner::class)
            authority(FinanceManager::class) -> user.addRole(FinanceManager::class)
            else -> throw NotFoundException("Role corresponding to right was not found")
        }

        userService.save(user)
        return UserView(user)
    }

    /**
     * DELETE /admin/{id}/makeadmin
     * Turn a user into an Admin
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("user/{id}/admin/")
    fun removeAdminRights(@PathVariable id: Long,
                          @RequestParam authority: String): UserView {

        val user = userService.getUserById(id) ?: throw NotFoundException("User with ID $id not found")


        fun <T : UserRole> authority(clazz: KClass<T>): String {
            return clazz.findAnnotation<DiscriminatorValue>()?.value ?: throw NotFoundException("Role corresponding to right was not found")
        }

        when (authority) {
            authority(Admin::class) -> user.removeRole(Admin::class)
            authority(EventManager::class) -> user.removeRole(EventManager::class)
            authority(EventOwner::class) -> user.removeRole(EventOwner::class)
            authority(FinanceManager::class) -> user.removeRole(FinanceManager::class)
            else -> throw NotFoundException("Role corresponding to right was not found")
        }

        userService.save(user)
        return UserView(user)
    }

    /**
     * POST /admin/{id}/swappasswords
     * Swaps your password with another user
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("user/{id}/swappasswords/")
    fun swapPasswords(@PathVariable id: Long,
                      @AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, String> {

        val first = userService.getUserFromCustomUserDetails(customUserDetails)
        val second = userService.getUserById(id) ?: throw NotFoundException("User with ID $id not found")

        userService.swapPasswords(first.account, second.account)

        return mapOf("message" to "success")
    }
}
