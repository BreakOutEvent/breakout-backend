package backend.model.payment

import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Admin
import backend.services.MailService
import backend.util.getBankingSubject
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
                    "Um vollständig angemeldet zu sein musst entweder Du oder Dein Teampartner noch 30€ an folgendes Konto überweisen:<br><br>" +
                    "Inhaber: 	Daria Brauner<br>" +
                    "IBAN: 		DE60 7002 2200 0072 7083 26<br>" +
                    "BIC: 		FDDODEMMXXX<br>" +
                    "Zweck:		${getBankingSubject(invoice.team!!.id!!, "VORNAME", "NACHNAME")}<br><br>" +
                    "Wenn das erledigt ist seid Ihr vollständig angemeldet!<br><br>" +
                    "Liebe Grüße<br>" +
                    "Euer BreakOut-Team"

    )

    private fun getFullyPaidEmail(invoice: TeamEntryFeeInvoice) = Email(
            to = invoice.team!!.members.map { EmailAddress(it.email) },
            subject = "BreakOut 2016 - Du bist Dabei!",
            body = "Liebes Team ${invoice.team!!.name},<br><br>" +
                    "Eure Startgebühr ist vollständig bei uns eingegnagen. Ihr seid jetzt vollständig bei BreakOut angemeldet, yuhuuu!! Jetzt gehts für Euch ans Sponsoren suchen. Über alles weitere halten wir Euch auf dem Laufenden!<br>" +
                    "Bis zum 3. Juni wir freuen uns auf Euch!<br><br>" +
                    "Liebe Grüße<br>" +
                    "Euer BreakOut-Team"
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

