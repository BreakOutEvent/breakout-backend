package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.TeamService
import backend.model.payment.SponsoringInvoiceService
import backend.model.payment.TeamEntryFeeService
import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.UserService
import backend.view.PaymentView
import backend.view.SponsoringInvoiceView
import backend.view.TeamEntryFeeInvoiceView
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.validation.Valid

@RestController
@RequestMapping("/invoice")
open class InvoiceController {

    private val teamEntryFeeService: TeamEntryFeeService
    private val sponsoringInvoiceService: SponsoringInvoiceService
    private val teamService: TeamService
    private val userService: UserService
    private val logger: Logger

    @Autowired
    constructor(teamEntryFeeService: TeamEntryFeeService, userService: UserService, sponsoringInvoiceService: SponsoringInvoiceService, teamService: TeamService) {
        this.teamEntryFeeService = teamEntryFeeService
        this.sponsoringInvoiceService = sponsoringInvoiceService
        this.userService = userService
        this.teamService = teamService
        this.logger = LoggerFactory.getLogger(InvoiceController::class.java)
    }

    /**
     * POST /invoice/{id}/payment/
     * Allows admin to add payment to given invoice
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/{invoiceId}/payment/", method = arrayOf(POST))
    open fun createPayment(@PathVariable invoiceId: Long,
                           @Valid @RequestBody paymentView: PaymentView,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): Any {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val teamFeeInvoice = teamEntryFeeService.findById(invoiceId)
        val sponsoringInvoice = sponsoringInvoiceService.findById(invoiceId)
        val admin = user.getRole(Admin::class) ?: throw UnauthorizedException("User is no admin")
        val amount = Money.of(BigDecimal.valueOf(paymentView.amount!!), "EUR")

        if (teamFeeInvoice != null) {
            val savedInvoice = teamEntryFeeService.addAdminPaymentToInvoice(admin, amount, teamFeeInvoice)
            return TeamEntryFeeInvoiceView(savedInvoice)
        }

        if (sponsoringInvoice != null) {
            val savedInvoice = sponsoringInvoiceService.addAdminPaymentToInvoice(admin, amount, sponsoringInvoice)
            return SponsoringInvoiceView(savedInvoice)
        }

        throw NotFoundException("No invoice with id $invoiceId found")
    }


    /**
     * GET /invoice/teamfee/{id}/
     * Allows admin to get given invoice
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/teamfee/{invoiceId}/", method = arrayOf(GET))
    open fun getTeamFeeInvoice(@PathVariable invoiceId: Long): TeamEntryFeeInvoiceView {

        val invoice = teamEntryFeeService.findById(invoiceId) ?: throw NotFoundException("No invoice with id $invoiceId found")
        return TeamEntryFeeInvoiceView(invoice)
    }

    /**
     * GET /invoice/sponsoring/{teamId}/
     * Get all sponsoring invoices by teamId
     * Allowed for admin & members of team
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/sponsoring/{teamId}/", method = arrayOf(GET))
    open fun getAllSponsorInvoicesForTeam(@PathVariable teamId: Long,
                                          @AuthenticationPrincipal cud: CustomUserDetails): Iterable<SponsoringInvoiceView> {
        val user = userService.getUserFromCustomUserDetails(cud)
        if(user.hasRole(Admin::class)) {
            val invoices = sponsoringInvoiceService.findByTeamId(teamId)
            return invoices.map(::SponsoringInvoiceView)
        }

        val team = teamService.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val participant = user.getRole(Participant::class) ?: throw UnauthorizedException("User not admin or member of team")

        if(team.isMember(participant)) {
            val invoices = sponsoringInvoiceService.findByTeamId(teamId)
            return invoices.map(::SponsoringInvoiceView)
        } else throw UnauthorizedException("User not part of team")
    }

    /**
     * GET /invoice/sponsoring/
     * Allows admin to get all sponsoring invoices
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/sponsoring/", method = arrayOf(GET))
    open fun getAllSponsorInvoices(): List<SponsoringInvoiceView> {

        val invoices = sponsoringInvoiceService.findAll()
        return invoices.map(::SponsoringInvoiceView)
    }

    /**
     * POST /invoice/sponsoring/
     * Allows admin to get all sponsoring invoices
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/sponsoring/", method = arrayOf(POST))
    open fun createSponsorInvoice(@Valid @RequestBody body: Map<String, Any>): SponsoringInvoiceView {

        val amount = body["amount"] as? Number ?: throw BadRequestException("body is missing field amount")
        val teamId = body["teamId"] as? Int ?: throw BadRequestException("body is missing field teamId")
        val firstname = body["firstname"] as? String ?: throw BadRequestException("body is missing field firstname")
        val lastname = body["lastname"] as? String ?: throw BadRequestException("body is missing field lastname")
        val company = body["company"] as? String ?: throw BadRequestException("body is missing field company")

        val team = teamService.findOne(teamId.toLong()) ?: throw BadRequestException("team not found")

        val sponsoringInvoice = sponsoringInvoiceService.createInvoice(team, Money.of(amount, "EUR"), company, firstname, lastname)
        return SponsoringInvoiceView(sponsoringInvoice)
    }
}
