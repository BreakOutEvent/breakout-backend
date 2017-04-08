package backend.model.payment

import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Admin
import backend.services.MailService
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        this.logger = LoggerFactory.getLogger(TeamEntryFeeServiceImpl::class.java)
        this.mailService = mailService
    }

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

    override fun addPaymentServicePaymentToInvoice(amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        TODO("not implemented")
    }
}

