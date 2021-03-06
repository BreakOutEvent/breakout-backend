package backend.model.payment

import backend.model.challenges.ChallengeService
import backend.model.event.Event
import backend.model.event.EventService
import backend.model.event.Team
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.SponsoringService
import backend.model.user.Admin
import backend.model.user.User
import backend.services.mail.MailService
import backend.util.euroOf
import org.javamoney.moneta.Money
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class SponsoringInvoiceServiceImpl(private val sponsoringInvoiceRepository: SponsoringInvoiceRepository,
                                   private val challengeService: ChallengeService,
                                   private val sponsoringService: SponsoringService,
                                   private val mailService: MailService,
                                   private val eventService: EventService) : SponsoringInvoiceService {

    private val logger = LoggerFactory.getLogger(SponsoringInvoiceServiceImpl::class.java)

    @Transactional
    override fun addAdminPaymentToInvoice(admin: User, amount: Money, invoice: SponsoringInvoice, date: LocalDateTime?, fidorId: Long?): SponsoringInvoice {
        val payment = AdminPayment(amount, admin, fidorId, date)
        invoice.addPayment(payment)
        return invoice
    }

    @Transactional
    override fun addSepaPaymentToInvoice(admin: User, fidorId: Long, amount: Money, date: LocalDateTime?, invoice: SponsoringInvoice): SponsoringInvoice {
        val payment = SepaPayment(amount, admin, fidorId, date)
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

    override fun findBySponsorId(sponsorId: Long): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.findBySponsorId(sponsorId)
    }

    override fun findAll(): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.findAll()
    }

    override fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.findByTeamId(teamId)
    }

    override fun findByEventId(eventId: Long): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.findAllByEventId(eventId)
    }

    override fun findByFilters(purposeOfTransferCode: String?,
                               teamId: Long?,
                               eventId: Long?,
                               firstname: String?,
                               lastname: String?,
                               company: String?,
                               minDonation: Money?,
                               maxDonation: Money?,
                               donorType: String?): Iterable<SponsoringInvoice> {

        val invoices = purposeOfTransferCode?.let {
            arrayOf(sponsoringInvoiceRepository.findByPurposeOfTransferCode(purposeOfTransferCode))
                    .mapNotNull { it }
        } ?: teamId?.let {
            sponsoringInvoiceRepository.findByTeamId(it)
        } ?: eventId?.let {
            sponsoringInvoiceRepository.findAllByEventId(it)
        } ?: sponsoringInvoiceRepository.findAll()

        return invoices
                .filterBy(purposeOfTransferCode) { purposeOfTransferCode, invoice -> purposeOfTransferCode == invoice.purposeOfTransferCode }
                .filterBy(eventId) { eventId, invoice -> eventId == invoice.event?.id }
                .filterBy(firstname) { firstname, invoice -> invoice.sponsor?.firstname?.contains(firstname, ignoreCase = true) ?: false }
                .filterBy(lastname) { lastname, invoice -> invoice.sponsor?.lastname?.contains(lastname, ignoreCase = true) ?: false }
                .filterBy(company) { company, invoice -> invoice.sponsor?.company?.contains(company, ignoreCase = true) ?: false }
                .filterBy(minDonation) { minDonation, invoice -> minDonation <= invoice.amount }
                .filterBy(maxDonation) { maxDonation, invoice -> maxDonation >= invoice.amount }
                .filterBy(donorType) { donorType, invoice -> donorType == invoice.sponsor?.supporterType?.name }

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

        val amountsMatch = total.numberStripped.compareTo(highscore.fullSum) == 0
        if (!amountsMatch) {
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

    override fun sendInvoiceReminderEmailsToSponsorsForEvent(event: Event) {

        val invoices = findAllNotFullyPaidInvoicesForEvent(event)

        invoices.forEach {
            mailService.sendGeneratedDonationPromiseReminderSponsor(it)
            Thread.sleep(1000)
        }

        logger.info("Sent payment reminder emails for event ${event.id} to ${invoices.count()} sponsors")
    }

    override fun findAllNotFullyPaidInvoicesForEvent(event: Event): Iterable<SponsoringInvoice> {
        return sponsoringInvoiceRepository.findAllByEventId(event.id!!)
                .filter { !it.isFullyPaid() }
    }

    override fun sendInvoiceReminderEmailsToTeamsForEvent(event: Event) {
        val grouped = findTeamInvoicePairs(event)
        // Send emails to all teams with an overview which sponsor has not fully paid yet
        grouped.forEach { team, invoices ->
            mailService.sendInvoiceReminderEmailsToTeam(team, invoices)
            Thread.sleep(1000)
        }
    }

    override fun findTeamInvoicePairs(event: Event): Map<Team, List<SponsoringInvoice>> {
        // Find all not fully paid invoices for a team
        val invoices = sponsoringInvoiceRepository
                .findAllByEventId(event.id!!)
                .filter { !it.isFullyPaid() }

        // Transform each invoice into a combination of List<Team, List<ISponsor>>

        return invoices
                .map(this::invoiceToTeamSponsorPairs)
                .flatten()
                .groupBy { it.first }
                .mapValues { it.value.map { value -> value.second } }
                .filter { it.key.hasStarted }
    }

    private fun invoiceToTeamSponsorPairs(invoice: SponsoringInvoice): List<Pair<Team, SponsoringInvoice>> {
        val teamsFromChallenges = invoice.challenges.map { it.team }
        val teamsFromSponsorings = invoice.sponsorings.map { it.team }
        val teamsFromBoth = teamsFromChallenges.union(teamsFromSponsorings).distinct()
        return teamsFromBoth.filterNotNull().map { it to invoice }
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

private fun <T, V> Iterable<T>.filterBy(value: V?, predicate: (V, T) -> Boolean): Iterable<T> {
    return value?.let { value -> filter { predicate(value, it) } } ?: this
}