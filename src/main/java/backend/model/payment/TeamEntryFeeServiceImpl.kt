package backend.model.payment

import org.apache.log4j.Logger
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

