package backend.model.payment

import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Admin
import backend.services.MailService
import org.apache.log4j.Logger
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class TeamEntryFeeServiceImpl : TeamEntryFeeService {

    private val teamEntryFeeInvoiceRepository: TeamEntryFeeInvoiceRepository
    private val mailService: MailService
    private val logger: Logger

    @Autowired
    constructor(teamEntryFeeInvoiceRepository: TeamEntryFeeInvoiceRepository, mailService: MailService) {
        this.teamEntryFeeInvoiceRepository = teamEntryFeeInvoiceRepository
        this.logger = Logger.getLogger(TeamEntryFeeServiceImpl::class.java)
        this.mailService = mailService
    }

    @Transactional
    override fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        val payment = AdminPayment(amount, admin)
        invoice.addPayment(payment)

        val email = if (invoice.isFullyPaid()) {
            getFullyPaidEmail(invoice)
        } else {
            getPartiallyPaidEmail(invoice)
        }

        mailService.send(email, false)
        return invoice
    }

    // TODO: Make Payment amount human readable
    private fun getPartiallyPaidEmail(invoice: TeamEntryFeeInvoice) = Email(
            to = invoice.team!!.members.map { EmailAddress(it.email) },
            subject = "BreakOut 2016 - Eine Zahlung ist bei uns eingegangen",
            body = "Hallo Team ${invoice.team!!.name},<br><br>" +
                    "Eine neue Zahlung für euer Team ist bei uns eingegangen. Ihr habt damit ${invoice.amountOfCurrentPayments()} von ${invoice.amount} bezahlt!<br><br>" +
                    "Vielen Dank<br>" +
                    "Euer BreakOut-Team" // TODO: Replace me
    )

    private fun getFullyPaidEmail(invoice: TeamEntryFeeInvoice) = Email(
            to = invoice.team!!.members.map { EmailAddress(it.email) },
            subject = "BreakOut 2016 - Startgebühr vollständig bezahlt",
            body = "Hallo Team ${invoice.team!!.name},<br><br>" +
                    "Eine neue Zahlung für euer Team ist bei uns eingegangen. Ihr habt damit die Startgebühr von ${invoice.amount} vollständig bezahlt!<br><br>" +
                    "Viel Spaß und viel Erfolg!<br>" +
                    "Euer BreakOut-Team" // TODO: Replace me
    )

    @Transactional
    override fun save(invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        return teamEntryFeeInvoiceRepository.save(invoice)
    }

    @Transactional
    override fun findById(id: Long): TeamEntryFeeInvoice? {
        return teamEntryFeeInvoiceRepository.findOne(id)
    }
}

