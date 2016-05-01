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

    private fun getPartiallyPaidEmail(invoice: TeamEntryFeeInvoice) = Email(
            to = invoice.team!!.members.map { EmailAddress(it.email) },
            subject = "BreakOut 2016 - Wir haben Eure Zahlung erhalten",
            body = "Liebes Team ${invoice.team!!.name},<br><br>" +
                    "schön dass Ihr Euch für BreakOut angemeldet habt. Bei uns ist bereits eine Zahlung von 30€ eingegangen. Für das gesamte Team ist aber eine Zahlung von 60€ fällig.<br>" +
                    "Um vollständig angemeldet zu sein müsst Ihr als Team bis zum 18. Mai noch weitere 30€ überweisen.<br>" +
                    "Wenn das erledigt ist seid Ihr vollständig angemeldet!<br><br>" +
                    "Liebe Grüße<br>" +
                    "Euer BreakOut-Team",
            buttonText = "JETZT ZAHLEN",
            buttonUrl = "https://anmeldung.break-out.org/payment?utm_source=backend&utm_medium=email&utm_content=half&utm_campaign=payment",
            campaignCode = "payment_half"

    )

    private fun getFullyPaidEmail(invoice: TeamEntryFeeInvoice) = Email(
            to = invoice.team!!.members.map { EmailAddress(it.email) },
            subject = "BreakOut 2016 - Du bist Dabei!",
            body = "Liebes Team ${invoice.team!!.name},<br><br>" +
                    "Eure Startgebühr ist vollständig bei uns eingegangen. Eure Anmelung bei BreakOut ist damit abgeschlossen! Infos über die nächsten Schritte findet ihr auf unserer Webseite.<br>" +
                    "Über alles weitere halten wir Euch über Facebook und gelegentliche E-Mails auf dem Laufenden!<br>" +
                    "Bis zum 3. Juni wir freuen uns auf Euch!<br><br>" +
                    "Liebe Grüße<br>" +
                    "Euer BreakOut-Team",
            buttonText = "NÄCHSTE SCHRITTE",
            buttonUrl = "http://www.break-out.org/naechste-schritte/?utm_source=backend&utm_medium=email&utm_content=full&utm_campaign=payment",
            campaignCode = "payment_full"
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

