package backend.model.payment

import backend.model.event.Event
import backend.model.event.Team
import backend.model.user.Admin
import backend.model.user.User
import org.javamoney.moneta.Money
import java.time.LocalDateTime

interface SponsoringInvoiceService {

    fun findById(id: Long): SponsoringInvoice?

    fun findBySponsorId(sponsorId: Long): Iterable<SponsoringInvoice>

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

    fun findAll(): Iterable<SponsoringInvoice>

    fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice>

    fun findByEventId(eventId: Long): Iterable<SponsoringInvoice>

    fun findByFilters(transferCode: String?, teamId: Long?, eventId: Long?, firstName: String?, lastName: String?, company: String?, minDonationSum: Money?, maxDonationSum: Money?, donorType: String?): Iterable<SponsoringInvoice>

    fun save(invoice: SponsoringInvoice): SponsoringInvoice

    fun addAdminPaymentToInvoice(admin: User, amount: Money, invoice: SponsoringInvoice, date: LocalDateTime?, fidorId: Long?): SponsoringInvoice

    fun createInvoicesForEvent(event: Event): Int

    fun addSepaPaymentToInvoice(admin: User, fidorId: Long, amount: Money, date: LocalDateTime?, invoice: SponsoringInvoice): SponsoringInvoice

    fun sendInvoiceEmailsToSponsorsForEvent(event: Event)

    fun sendInvoiceReminderEmailsToSponsorsForEvent(event: Event)

    fun saveAll(invoices: Iterable<SponsoringInvoice>): Iterable<SponsoringInvoice>

    fun findAllNotFullyPaidInvoicesForEvent(event: Event): Iterable<SponsoringInvoice>

    fun sendInvoiceReminderEmailsToTeamsForEvent(event: Event)

    fun findTeamInvoicePairs(event: Event): Map<Team, List<SponsoringInvoice>>
}
