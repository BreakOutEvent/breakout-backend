package backend.model.payment

import backend.exceptions.DomainException
import backend.model.user.Admin
import backend.model.user.User
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
    override fun addAdminPaymentToInvoice(user: User, amount: Money, invoice: TeamEntryFeeInvoice): Invoice {
        val admin = user.getRole(Admin::class) ?: throw DomainException("User ${user.core.id} can't add payments because he is no Admin")
        val payment = AdminPayment(amount, admin)
        invoice.addPayment(payment)
        return invoice

        //TODO: Make sure emails are sent here!
    }

    @Transactional
    override fun save(invoice: TeamEntryFeeInvoice) {
        teamEntryFeeInvoiceRepository.save(invoice)
    }

    @Transactional
    override fun findById(id: Long): TeamEntryFeeInvoice? {
        return teamEntryFeeInvoiceRepository.findOne(id)
    }
}

