package backend.model.payment

import org.springframework.data.repository.CrudRepository

interface SponsoringInvoiceRepository : CrudRepository<SponsoringInvoice, Long> {

    fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice>

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

}
