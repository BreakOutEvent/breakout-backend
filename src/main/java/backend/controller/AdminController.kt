package backend.controller

import backend.configuration.CustomUserDetails
import backend.services.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
open class AdminController {


    private val mailService: MailService

    @Autowired
    constructor(mailService: MailService) {
        this.mailService = mailService
    }

    /**
     * GET /me/
     * Get information to the currently authenticated user
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/resendmail")
    open fun getAuthenticatedUser(@AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, Int> {
        val count = mailService.resendFailed()
        return mapOf("count" to count)
    }

}
