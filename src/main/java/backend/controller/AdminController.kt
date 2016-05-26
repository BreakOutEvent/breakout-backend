package backend.controller

import backend.configuration.CustomUserDetails
import backend.model.event.TeamService
import backend.services.MailService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController

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
        this.logger = LoggerFactory.getLogger(AdminController::class.java)

    }

    /**
     * GET /admin/resendmail/
     * Allows Admin to resend failed mails
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/resendmail/", method = arrayOf(GET))
    open fun resendMail(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, Int> {
        val count = mailService.resendFailed()

        logger.info("Resent $count mails from admin request")
        return mapOf("count" to count)
    }
}
