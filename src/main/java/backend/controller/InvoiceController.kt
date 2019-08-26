package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.TeamService
import backend.model.payment.SponsoringInvoiceService
import backend.model.payment.TeamEntryFeeService
import backend.model.user.*
import backend.services.ConfigurationService
import backend.view.PaymentView
import backend.view.TeamEntryFeeInvoiceView
import backend.view.sponsoring.DetailedSponsoringInvoiceView
import backend.view.sponsoring.SponsoringInvoiceView
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.Valid

@RestController
@RequestMapping("/invoice")
class InvoiceController(private val teamEntryFeeService: TeamEntryFeeService,
                        private val userService: UserService,
                        private val sponsoringInvoiceService: SponsoringInvoiceService,
                        private val teamService: TeamService,
                        private val configurationService: ConfigurationService) {

    private val logger: Logger = LoggerFactory.getLogger(InvoiceController::class.java)
    private val PAYMENT_AUTH_TOKEN: String = configurationService.getRequired("org.breakout.api.payment_auth_token")

    /**
     * POST /invoice/{id}/payment/
     * Allows admin to add payment to given invoice
     */
    @PreAuthorize("hasAuthority('FINANCE_MANAGER')")
    @PostMapping("/{invoiceId}/payment/")
    fun createAdminPayment(@PathVariable invoiceId: Long,
                           @Valid @RequestBody paymentView: PaymentView,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): Any {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val teamFeeInvoice = teamEntryFeeService.findById(invoiceId)
        val sponsoringInvoice = sponsoringInvoiceService.findById(invoiceId)

        val amount = Money.of(BigDecimal.valueOf(paymentView.amount!!), "EUR")
        val date: LocalDateTime? = paymentView.date?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC) }

        if (teamFeeInvoice != null) {
            val savedInvoice = teamEntryFeeService.addAdminPaymentToInvoice(user, amount, teamFeeInvoice, date, paymentView.fidorId)
            return TeamEntryFeeInvoiceView(savedInvoice)
        }

        if (sponsoringInvoice != null) {
            val savedInvoice = sponsoringInvoiceService.addAdminPaymentToInvoice(user, amount, sponsoringInvoice, date, paymentView.fidorId)
            return SponsoringInvoiceView(savedInvoice)
        }

        throw NotFoundException("No invoice with id $invoiceId found")
    }


    /**
     * POST /invoice/payment/{purposeOfTransferCode}/
     * Allows admin to add payment to given invoice
     */
    @PostMapping("/payment/{purposeOfTransferCode}/")
    fun createPayment(@PathVariable purposeOfTransferCode: String,
                      @RequestHeader("X-AUTH-TOKEN") authToken: String,
                      @Valid @RequestBody paymentView: PaymentView): Any {

        if (authToken != PAYMENT_AUTH_TOKEN) throw UnauthorizedException("Invalid Payment-Auth Token")

        val teamFeeInvoice = teamEntryFeeService.findByPurposeOfTransferCode(purposeOfTransferCode)
        val sponsoringInvoice = sponsoringInvoiceService.findByPurposeOfTransferCode(purposeOfTransferCode)
        val amount = Money.of(BigDecimal.valueOf(paymentView.amount!!), "EUR")

        //TODO get admin in a nicer way
        val admin = userService.getUserById(1)!!.getRole(Admin::class)
                ?: throw UnauthorizedException("User is no admin")
        if (paymentView.fidorId == null) throw RuntimeException("No fidorId is set for automatic payment insertion")

        val date: LocalDateTime? = paymentView.date?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC) }

        if (teamFeeInvoice != null) {
            val savedInvoice = teamEntryFeeService.addSepaPaymentToInvoice(admin, paymentView.fidorId!!, amount, date, teamFeeInvoice)
            return TeamEntryFeeInvoiceView(savedInvoice)
        }

        if (sponsoringInvoice != null) {
            val savedInvoice = sponsoringInvoiceService.addSepaPaymentToInvoice(admin, paymentView.fidorId!!, amount, date, sponsoringInvoice)
            return SponsoringInvoiceView(savedInvoice)
        }

        throw NotFoundException("No invoice with purpose of transfer code $purposeOfTransferCode found")

    }

    /**
     * GET /invoice/payment/
     * Allows admin to get all payments
     */
    @GetMapping("/payment/")
    fun getPayments(@RequestHeader("X-AUTH-TOKEN") authToken: String): List<PaymentView> {

        if (authToken != PAYMENT_AUTH_TOKEN) throw UnauthorizedException("Invalid Payment-Auth Token")

        val teamEntryFeePayments = teamEntryFeeService.findAll().flatMap { it.getPayments() }
        val sponsoringInvoicePayments = sponsoringInvoiceService.findAll().flatMap { it.getPayments() }

        return (teamEntryFeePayments + sponsoringInvoicePayments).map(::PaymentView)
    }


    /**
     * GET /invoice/teamfee/{id}/
     * Allows admin to get given invoice
     */
    @PreAuthorize("hasAuthority('FINANCE_MANAGER')")
    @GetMapping("/teamfee/{invoiceId}/")
    fun getTeamFeeInvoice(@PathVariable invoiceId: Long): TeamEntryFeeInvoiceView {

        val invoice = teamEntryFeeService.findById(invoiceId)
                ?: throw NotFoundException("No invoice with id $invoiceId found")
        return TeamEntryFeeInvoiceView(invoice)
    }

    /**
     * GET /invoice/sponsoring/{teamId}/
     * Get all sponsoring invoices by teamId
     * Allowed for admin & members of team
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sponsoring/{teamId}/")
    fun getAllSponsorInvoicesForTeam(@PathVariable teamId: Long,
                                     @AuthenticationPrincipal cud: CustomUserDetails): Iterable<SponsoringInvoiceView> {
        val user = userService.getUserFromCustomUserDetails(cud)
        if (user.hasAuthority(FinanceManager::class)) {
            val invoices = sponsoringInvoiceService.findByTeamId(teamId)
            return invoices.map(::SponsoringInvoiceView)
        }

        val team = teamService.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val participant = user.getRole(Participant::class)
                ?: throw UnauthorizedException("User not admin or member of team")

        if (team.isMember(participant)) {
            return sponsoringInvoiceService
                    .findByTeamId(teamId)
                    .filter { it.amount.isPositive }
                    .map(::SponsoringInvoiceView)
        } else throw UnauthorizedException("User not part of team")
    }

    /**
     * GET /invoice/sponsoring/
     * Allows admin & sponsors to get all sponsoring invoices
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sponsoring/")
    fun getAllSponsorInvoices(@AuthenticationPrincipal cud: CustomUserDetails): List<DetailedSponsoringInvoiceView> {
        val user = userService.getUserFromCustomUserDetails(cud)

        val sponsor = user.getRole(Sponsor::class)
                ?: throw UnauthorizedException("User not a sponsor")

        return sponsoringInvoiceService
                .findBySponsorId(sponsor.id!!)
                .filter { it.amount.isPositive }
                .map(::DetailedSponsoringInvoiceView)
    }

    /**
     * GET /invoice/sponsoring/
     * Allows admin to get all sponsoring invoices
     */
    @PreAuthorize("hasAuthority('FINANCE_MANAGER')")
    @GetMapping("/sponsoring/admin/")
    fun getAllSponsorInvoices(): List<SponsoringInvoiceView> {
        val invoices = sponsoringInvoiceService.findAll()
        return invoices.map(::SponsoringInvoiceView)

    }
}
