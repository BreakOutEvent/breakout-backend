package backend.model.payment

import backend.model.challenges.Challenge
import backend.model.event.Team
import backend.model.sponsoring.Sponsoring
import backend.model.user.Admin
import org.javamoney.moneta.Money

interface SponsoringInvoiceService {

    fun findById(id: Long): SponsoringInvoice?

    fun findByPurposeOfTransferCode(purposeOfTransferCode:String): SponsoringInvoice?

    fun findAll(): Iterable<SponsoringInvoice>

    fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice>

    fun save(invoice: SponsoringInvoice): SponsoringInvoice

    fun addAdminPaymentToInvoice(admin: Admin, amount: Money, invoice: SponsoringInvoice): SponsoringInvoice

    fun addPaymentServicePaymentToInvoice(amount: Money, invoice: SponsoringInvoice): SponsoringInvoice

    fun createInvoice(team: Team, amount: Money, subject: String, sponsorings: List<Sponsoring>, challenges: List<Challenge>): SponsoringInvoice

    fun createInvoice(team: Team, amount: Money, company: String, firstname: String, lastname: String): SponsoringInvoice

}
