package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.payment.SponsoringInvoiceService
import backend.model.payment.TeamEntryFeeService
import backend.model.user.Admin
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
    private val userService: UserService
    private val logger: Logger

    @Autowired
    constructor(teamEntryFeeService: TeamEntryFeeService, userService: UserService, sponsoringInvoiceService: SponsoringInvoiceService) {
        this.teamEntryFeeService = teamEntryFeeService
        this.sponsoringInvoiceService = sponsoringInvoiceService
        this.userService = userService
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
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): TeamEntryFeeInvoiceView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val invoice = teamEntryFeeService.findById(invoiceId) ?: throw NotFoundException("No invoice with id $invoiceId found")
        val admin = user.getRole(Admin::class) ?: throw UnauthorizedException("User is no admin")
        val amount = Money.of(BigDecimal.valueOf(paymentView.amount!!), "EUR")

        val savedInvoice = teamEntryFeeService.addAdminPaymentToInvoice(admin, amount, invoice)
        val responseView = TeamEntryFeeInvoiceView(savedInvoice)
        return responseView
    }


    /**
     * GET /invoice/teamfee/{id}/
     * Allows admin to get given invoice
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/teamfee/{invoiceId}/", method = arrayOf(GET))
    open fun getTeamFeeInvoice(@PathVariable invoiceId: Long,
                               @AuthenticationPrincipal customUserDetails: CustomUserDetails): TeamEntryFeeInvoiceView {

        val invoice = teamEntryFeeService.findById(invoiceId) ?: throw NotFoundException("No invoice with id $invoiceId found")
        return TeamEntryFeeInvoiceView(invoice)
    }

    /**
     * GET /invoice/sponsoring/
     * Allows admin to get all sponsoringinvoices
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/sponsoring/", method = arrayOf(GET))
    open fun getAllSponsorInvoices(@AuthenticationPrincipal customUserDetails: CustomUserDetails): List<SponsoringInvoiceView> {

        val invoices = sponsoringInvoiceService.findAll()
        return invoices.map { SponsoringInvoiceView(it) }
    }
}
