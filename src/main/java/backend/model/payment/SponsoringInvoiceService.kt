package backend.model.payment

import backend.model.challenges.Challenge
import backend.model.event.Event
import backend.model.event.Team
import backend.model.sponsoring.Sponsoring
import backend.model.user.Admin
import org.javamoney.moneta.Money

interface SponsoringInvoiceService {

    fun findById(id: Long): SponsoringInvoice?

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

    fun findAll(): Iterable<SponsoringInvoice>

    fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice>

    fun save(invoice: SponsoringInvoice): SponsoringInvoice

    fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: SponsoringInvoice): SponsoringInvoice

    @Deprecated("Legacy method used for previous versions, where SponsoringInvoices where generated on a per team basis")
    fun createInvoice(team: Team, amount: Money, subject: String, sponsorings: List<Sponsoring>, challenges: List<Challenge>): SponsoringInvoice

    @Deprecated("Legacy method used for previous versions, where SponsoringInvoices where generated on a per team basis")
    fun createInvoice(team: Team, amount: Money, company: String, firstname: String, lastname: String): SponsoringInvoice

    fun createInvoicesForEvent(event: Event): Int

    fun addSepaPaymentToInvoice(admin: Admin,fidorId: Long, amount: Money, invoice: SponsoringInvoice): SponsoringInvoice

    fun sendInvoiceEmailsToSponsorsForEvent(event: Event)

    fun saveAll(invoices: Iterable<SponsoringInvoice>): Iterable<SponsoringInvoice>
}
