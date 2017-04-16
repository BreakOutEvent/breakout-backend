package backend.model.payment

import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeRepository
import backend.model.event.Team
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringRepository
import backend.model.user.Admin
import backend.services.mail.MailService
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SponsoringInvoiceServiceImpl : SponsoringInvoiceService {

    private val sponsoringInvoiceRepository: SponsoringInvoiceRepository
    private val sponsoringRepository: SponsoringRepository
    private val challengeRepository: ChallengeRepository
    private val mailService: MailService
    private val logger: Logger

    @Autowired
    constructor(sponsoringInvoiceRepository: SponsoringInvoiceRepository, mailService: MailService, sponsoringRepository: SponsoringRepository, challengeRepository: ChallengeRepository) {
        this.sponsoringInvoiceRepository = sponsoringInvoiceRepository
        this.sponsoringRepository = sponsoringRepository
        this.challengeRepository = challengeRepository
        this.logger = LoggerFactory.getLogger(TeamEntryFeeServiceImpl::class.java)
        this.mailService = mailService
    }

    @Transactional
    override fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: SponsoringInvoice): SponsoringInvoice {
        val payment = AdminPayment(amount, admin)
        invoice.addPayment(payment)
        return invoice
    }

    @Transactional
    override fun addSepaPaymentToInvoice(admin: Admin, fidorId: Long, amount: Money, invoice: SponsoringInvoice): SponsoringInvoice {
        val payment = SepaPayment(amount, admin, fidorId)
        invoice.addPayment(payment)
        return invoice
    }

    @Transactional
    override fun save(invoice: SponsoringInvoice): SponsoringInvoice {
        return sponsoringInvoiceRepository.save(invoice)
    }

    @Transactional
    override fun findById(id: Long): SponsoringInvoice? {
        return sponsoringInvoiceRepository.findOne(id)
    }

    override fun findAll(): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.findAll()
    }

    override fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.findByTeamId(teamId)
    }

    @Transactional
    override fun createInvoice(team: Team, amount: Money, subject: String, sponsorings: List<Sponsoring>, challanges: List<Challenge>): SponsoringInvoice {
        val invoice = SponsoringInvoice(team, amount, subject, sponsorings, challanges)
        val savedInvoice = sponsoringInvoiceRepository.save(invoice)

        sponsorings.forEach {
            it.invoice = savedInvoice
            sponsoringRepository.save(it)
        }
        challanges.forEach {
            it.invoice = savedInvoice
            challengeRepository.save(it)
        }

        return savedInvoice
    }

    @Transactional
    override fun createInvoice(team: Team, amount: Money, company: String, firstname: String, lastname: String): SponsoringInvoice {
        val invoice = SponsoringInvoice(team, amount, company, firstname, lastname)
        val savedInvoice = sponsoringInvoiceRepository.save(invoice)

        return savedInvoice
    }

    override fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice? {
        return sponsoringInvoiceRepository.findByPurposeOfTransferCode(purposeOfTransferCode)
    }
}

