package backend.model.payment

import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeService
import backend.model.event.Event
import backend.model.event.EventService
import backend.model.event.Team
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringService
import backend.model.user.Admin
import backend.services.mail.MailService
import backend.util.euroOf
import org.javamoney.moneta.Money
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SponsoringInvoiceServiceImpl(private val sponsoringInvoiceRepository: SponsoringInvoiceRepository,
                                   private val challengeService: ChallengeService,
                                   private val sponsoringService: SponsoringService,
                                   private val mailService: MailService,
                                   private val eventService: EventService) : SponsoringInvoiceService {

    private val logger = LoggerFactory.getLogger(SponsoringInvoiceServiceImpl::class.java)

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

    override fun saveAll(invoices: Iterable<SponsoringInvoice>): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.save(invoices)
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
        return sponsors.map { SponsoringInvoice(it, event) }
                .apply { sanityCheck(this, event) }
                .map { tryRetryAndLogFailure(it) }.count()

    }

    private fun tryRetryAndLogFailure(invoice: SponsoringInvoice) {
        // TODO: Fix this, but our shortened UUID sometimes has collisions right now
        try {
            this.save(invoice)
        } catch (e: Exception) {
            try {
                invoice.generatePurposeOfTransfer()
                this.save(invoice)
            } catch (e: Exception) {
                logger.error("Could not save invoice for sponsor ${invoice.registeredSponsor?.id} ${invoice.registeredSponsor?.email} ${invoice.unregisteredSponsor?.id} ${invoice.unregisteredSponsor?.firstname} ${invoice.unregisteredSponsor?.firstname} ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun sanityCheck(invoices: Iterable<SponsoringInvoice>, event: Event) {
        val total = invoices.fold(euroOf(0.0)) { a, b -> a.add(b.amount) }
        val highscore = eventService.getDonateSum(event.id!!)

        if (total.numberStripped != highscore.fullSum) {
            throw Exception("Sanity check failed. Amount in invoices (${total.numberStripped}) and highscore (${highscore.fullSum}) don't match")
        } else {
            logger.info("Sanity check succeeded. Creating invoices for a total of ${total.numberStripped}€")
        }
    }

    override fun sendInvoiceEmailsToSponsorsForEvent(event: Event) {
        val invoices = sponsoringInvoiceRepository.findByEventIdWhereInitialVersionSentIsFalse(event.id!!)
        invoices.forEach {
            mailService.sendGeneratedDonationPromiseSponsor(it)
            Thread.sleep(1000) // We otherwise might kill our own email server this way
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

