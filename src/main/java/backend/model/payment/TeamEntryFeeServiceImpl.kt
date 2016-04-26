package backend.model.payment

import backend.model.user.Admin
import org.apache.log4j.Logger
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class TeamEntryFeeServiceImpl : TeamEntryFeeService {

    private val teamEntryFeeInvoiceRepository: TeamEntryFeeInvoiceRepository
    private val logger: Logger

    @Autowired
    constructor(teamEntryFeeInvoiceRepository: TeamEntryFeeInvoiceRepository) {
        this.teamEntryFeeInvoiceRepository = teamEntryFeeInvoiceRepository
        this.logger = Logger.getLogger(TeamEntryFeeServiceImpl::class.java)
    }

    @Transactional
    override fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        val payment = AdminPayment(amount, admin)
        invoice.addPayment(payment)
        return invoice
        //TODO: Make sure emails are sent here!
    }

    @Transactional
    override fun save(invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice {
        return teamEntryFeeInvoiceRepository.save(invoice)
    }

    @Transactional
    override fun findById(id: Long): TeamEntryFeeInvoice? {
        return teamEntryFeeInvoiceRepository.findOne(id)
    }
}

