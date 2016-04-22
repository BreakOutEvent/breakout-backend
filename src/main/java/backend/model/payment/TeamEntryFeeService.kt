package backend.model.payment

import backend.model.user.User
import com.braintreegateway.BraintreeGateway
import com.braintreegateway.TransactionRequest
import org.apache.log4j.Logger
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

interface TeamEntryFeeService {
    fun findById(id: Long): Invoice?
    fun save(invoice: TeamEntryFeeInvoice)
    fun addPaymentToInvoice(invoice: Invoice, payment: Payment)
    fun addBraintreePaymentToInvoice(invoice: Invoice, user: User, amount: Money, nonce: String)
}

@Service
class TeamEntryFeeServiceImpl : TeamEntryFeeService {

    private val teamEntryFeeInvoiceRepository: TeamEntryFeeInvoiceRepository
    private val braintreeGateway: BraintreeGateway
    private val logger: Logger

    @Autowired
    constructor(teamEntryFeeInvoiceRepository: TeamEntryFeeInvoiceRepository, braintreeGateway: BraintreeGateway) {
        this.teamEntryFeeInvoiceRepository = teamEntryFeeInvoiceRepository
        this.logger = Logger.getLogger(TeamEntryFeeServiceImpl::class.java)
        this.braintreeGateway = braintreeGateway
    }

    @Transactional
    override fun addBraintreePaymentToInvoice(invoice: Invoice, user: User, amount: Money, nonce: String) {

        val amountAsBigDecimal = amount.numberStripped
        val transactionRequest = TransactionRequest()
                .amount(amountAsBigDecimal)
                .paymentMethodNonce(nonce)
                .descriptor()
                .name("Breakout 2017") // TODO: This needs to be changed into Configuration
                .phone("1234567890")
                .url("http://break-out.org")
                .done()


        val transactionResult = braintreeGateway.transaction().sale(transactionRequest).transaction
        val payment = BraintreePayment(Money.of(amountAsBigDecimal, "EUR"), user, transactionResult)
        this.addPaymentToInvoice(invoice, payment)
        logger.info("Created a new braintree payment and transaction")
    }

    @Transactional
    override fun addPaymentToInvoice(invoice: Invoice, payment: Payment) {
        invoice.addPayment(payment)
        // TODO: Check whether this is saved automatically thanks to @Transactional
    }

    @Transactional
    override fun save(invoice: TeamEntryFeeInvoice) {
        teamEntryFeeInvoiceRepository.save(invoice)
    }

    @Transactional
    override fun findById(id: Long): Invoice? {
        return teamEntryFeeInvoiceRepository.findOne(id)
    }
}
