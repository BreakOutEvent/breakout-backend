package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.model.payment.Invoice
import backend.model.payment.InvoiceService
import backend.model.user.UserService
import com.braintreegateway.BraintreeGateway
import org.apache.log4j.Logger
import org.codehaus.jackson.annotate.JsonProperty
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/invoice")
open class InvoiceController {

    private val braintreeGateway: BraintreeGateway
    private val invoiceService: InvoiceService
    private val userService: UserService
    private val logger: Logger

    @Autowired
    constructor(invoiceService: InvoiceService, braintreeGateway: BraintreeGateway, userService: UserService) {
        this.braintreeGateway = braintreeGateway
        this.invoiceService = invoiceService
        this.userService = userService
        this.logger = Logger.getLogger(InvoiceController::class.java)
    }

    /**
     * GET /invoice/{id}/payment/braintree/client_token/
     * Return a token for a braintree transaction
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/payment/braintree/client_token/")
    open fun getToken(@PathVariable("id") invoiceId: Long): Map<String, String> {
        invoiceService.findById(invoiceId) ?: throw NotFoundException("No invoice with id $invoiceId found")
        val token = braintreeGateway.clientToken().generate()
        return mapOf("token" to token)
    }

    /**
     * POST /invoice/{id}/payment/braintee/checkout/
     * Complete a braintree transaction with a nonce received from the braintree servers
     * TODO: Somehow the amount must be passed here!
     */
    @RequestMapping("/{id}/payment/braintree/checkout/", method = arrayOf(POST))
    open fun checkoutPaymentForInvoiceWithBraintree(@PathVariable("id") invoiceId: Long,
                                                    @ModelAttribute body: CheckoutForm,
                                                    @AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, String> {
        // Get all data
        val amount = Money.of(BigDecimal.valueOf(60.0), "EUR") // TODO: How do I get the amount here?
        val user = userService.getUserFromCustomUserDetails(customUserDetails) // TODO: How do I get the authenticated user here?
        val invoice: Invoice = invoiceService.findById(invoiceId) ?: throw NotFoundException("No invoice with id $invoiceId found")
        val nonce = body.nonce ?: throw BadRequestException("Missing nonce in body")

        invoiceService.addBraintreePaymentToInvoice(invoice, user, amount, nonce)
        return mapOf("message" to "success")
    }

    class CheckoutForm {
        @JsonProperty("payment_method_nonce")
        var nonce: String? = null
    }
}
