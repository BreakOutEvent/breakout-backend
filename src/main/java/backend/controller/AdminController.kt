package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.challenges.ChallengeService
import backend.model.event.TeamService
import backend.model.location.LocationService
import backend.model.misc.Coord
import backend.model.misc.EmailRepository
import backend.model.payment.SponsoringInvoiceService
import backend.model.posting.PostingService
import backend.model.sponsoring.SponsoringService
import backend.model.user.UserService
import backend.services.mail.MailService
import backend.view.posting.PostingView
import backend.view.user.AdminTeamLocationView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@RestController
@RequestMapping("/admin")
class AdminController(private val mailService: MailService,
                      private val teamService: TeamService,
                      private val sponsoringService: SponsoringService,
                      private val challengeService: ChallengeService,
                      private val userService: UserService,
                      private val emailRepository: EmailRepository,
                      private val locationService: LocationService,
                      private val sponsoringInvoiceService: SponsoringInvoiceService,
                      private val postingService: PostingService) {


    private val logger: Logger = LoggerFactory.getLogger(AdminController::class.java)

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/generatespeedtolocations/")
    fun generateSpeedToLocations(): String {
        logger.info("Regenerating speed to locations where missing from admin request")
        locationService.generateSpeed()
        return "done"
    }

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
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/postteamlocation/")
    fun postTeamLocation(@Valid @RequestBody body: AdminTeamLocationView): PostingView {
        val team = teamService.findOne(body.teamId) ?: throw NotFoundException("Team with ID ${body.teamId} not found")
        return PostingView(
                postingService.adminCreatePosting(
                        team.members.elementAt(0), "Current Location: ${body.city}", null, Coord(body.latitude, body.longitude), LocalDateTime.now()), null, null)
    }
}
