package backend.model.payment

import backend.model.user.Admin
import backend.services.mail.MailService
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class TeamEntryFeeServiceImpl(private val teamEntryFeeInvoiceRepository: TeamEntryFeeInvoiceRepository,
                              private val mailService: MailService) : TeamEntryFeeService {

    private val logger: Logger = LoggerFactory.getLogger(TeamEntryFeeServiceImpl::class.java)

    @Transactional
    override fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        val payment = AdminPayment(amount, admin)
        invoice.addPayment(payment)

        if (invoice.isFullyPaid()) {
            mailService.sendTeamHasPaidEmail(invoice)
        }

        return invoice
    }

    @Transactional
    override fun save(invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        return teamEntryFeeInvoiceRepository.save(invoice)
    }

    @Transactional
    override fun findById(id: Long): TeamEntryFeeInvoice? {
        return teamEntryFeeInvoiceRepository.findOne(id)
    }

    @Transactional
    override fun findByPurposeOfTransferCode(purposeOfTransferCode: String): TeamEntryFeeInvoice? {
        return teamEntryFeeInvoiceRepository.findByPurposeOfTransferCode(purposeOfTransferCode)
    }

    @Transactional
    override fun addSepaPaymentToInvoice(admin: Admin, fidorId: Long, amount: Money, date: LocalDateTime?, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        val payment = SepaPayment(amount, admin, fidorId, date)
        invoice.addPayment(payment)

        if (invoice.isFullyPaid()) {
            mailService.sendTeamHasPaidEmail(invoice)
        }

        return invoice
    }

    override fun findAll(): Iterable<TeamEntryFeeInvoice> {
        return teamEntryFeeInvoiceRepository.findAll()
    }

}

