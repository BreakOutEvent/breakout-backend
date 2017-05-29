package backend.model.payment

import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeService
import backend.model.event.Event
import backend.model.event.Team
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringService
import backend.model.user.Admin
import backend.services.mail.MailSenderService
import backend.services.mail.MailService
import org.javamoney.moneta.Money
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SponsoringInvoiceServiceImpl(private val sponsoringInvoiceRepository: SponsoringInvoiceRepository,
                                   private val challengeService: ChallengeService,
                                   private val sponsoringService: SponsoringService,
                                   private val mailService: MailService) : SponsoringInvoiceService {

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
    override fun createInvoice(team: Team, amount: Money, subject: String, sponsorings: List<Sponsoring>, challenges: List<Challenge>): SponsoringInvoice {
        val invoice = SponsoringInvoice(team, amount, subject, sponsorings, challenges)
        val savedInvoice = sponsoringInvoiceRepository.save(invoice)

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


    override fun createInvoicesForEvent(event: Event): Int {
        val sponsors = this.findAllSponsorsAtEvent(event.id!!)
        return sponsors.map { SponsoringInvoice(it, event) }.map { this.save(it) }.count()
    }

    override fun sendInvoiceEmailsToSponsorsForEvent(event: Event) {
        val invoices = sponsoringInvoiceRepository.findByEventId(event.id!!)
        invoices.forEach {
            mailService.sendGeneratedDonationPromiseSponsor(it)
        }
    }

    private fun findAllSponsorsAtEvent(eventId: Long): Iterable<ISponsor> {

        val registeredFromChallenges = challengeService.findAllRegisteredSponsorsWithChallengesAtEvent(eventId)
        val unregisteredFromChallenges = challengeService.findAllUnregisteredSponsorsWithChallengesAtEvent(eventId)

        val registeredFromSponsorings = sponsoringService.findAllRegisteredSponsorsWithSponsoringAtEvent(eventId)
        val unregisteredFromSponsorings = sponsoringService.findAllUnregisteredSponsorsWithSponsoringAtEvent(eventId)

        val set = mutableSetOf<ISponsor>()

        set.addAll(registeredFromSponsorings)
        set.addAll(unregisteredFromSponsorings)

        set.addAll(registeredFromChallenges)
        set.addAll(unregisteredFromChallenges)

        return set
    }

}

