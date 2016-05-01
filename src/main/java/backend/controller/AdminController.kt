package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.model.event.TeamService
import backend.model.payment.TeamEntryFeeInvoice
import backend.services.MailService
import org.apache.log4j.Logger
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/admin")
open class AdminController {


    private val mailService: MailService
    private val teamService: TeamService
    private val logger: Logger

    @Autowired
    constructor(mailService: MailService, teamService: TeamService) {
        this.mailService = mailService
        this.teamService = teamService
        this.logger = Logger.getLogger(AdminController::class.java)

    }

    /**
     * GET /admin/resendmail/
     * Allows Admin to resend failed mails
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/resendmail/", method = arrayOf(GET))
    open fun resendMail(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, Int> {
        val count = mailService.resendFailed()

        logger.info("Resent $count mails from admin request")
        return mapOf("count" to count)
    }

    /**
     * GET /admin/fullteam/{teamId}/
     * Allows Admin to add invoices to teams missing it and send full team mail
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/fullteam/{teamId}/", method = arrayOf(GET))
    open fun redoFullTeam(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                          @PathVariable("teamId") teamId: Long): Map<String, String> {

        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")

        if (team.members.size == 2 && team.invoice == null) {
            team.invoice = TeamEntryFeeInvoice(team, Money.of(BigDecimal.valueOf(60), "EUR"))
            teamService.save(team)

            val emails = teamService.getFullTeamMailForMember(team.members)
            emails.forEach { email ->
                mailService.send(email)
            }
        } else {
            throw NotFoundException("Team doesn't have two members, or already has invoice")
        }

        return mapOf("message" to "success")
    }

}
