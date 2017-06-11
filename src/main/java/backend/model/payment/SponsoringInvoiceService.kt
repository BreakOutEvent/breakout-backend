package backend.model.payment

import backend.model.event.Event
import backend.model.event.Team
import backend.model.user.Admin
import org.javamoney.moneta.Money

interface SponsoringInvoiceService {

    fun findById(id: Long): SponsoringInvoice?

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

    fun findAll(): Iterable<SponsoringInvoice>

    fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice>

    fun findByEventId(eventId: Long): Iterable<SponsoringInvoice>

    fun save(invoice: SponsoringInvoice): SponsoringInvoice

    fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: SponsoringInvoice, fidorId: Long? = null): SponsoringInvoice

    fun createInvoicesForEvent(event: Event): Int

    fun addSepaPaymentToInvoice(admin: Admin, fidorId: Long, amount: Money, invoice: SponsoringInvoice): SponsoringInvoice

    fun sendInvoiceEmailsToSponsorsForEvent(event: Event)

    fun sendInvoiceReminderEmailsToSponsorsForEvent(event: Event)

    fun saveAll(invoices: Iterable<SponsoringInvoice>): Iterable<SponsoringInvoice>

    fun findAllNotFullyPaidInvoicesForEvent(event: Event): Iterable<SponsoringInvoice>

    fun sendInvoiceReminderEmailsToTeamsForEvent(event: Event)
    fun findTeamInvoicePairs(event: Event): Map<Team, List<SponsoringInvoice>>
}
