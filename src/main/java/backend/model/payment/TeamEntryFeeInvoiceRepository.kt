package backend.model.payment

import org.springframework.data.repository.CrudRepository

interface TeamEntryFeeInvoiceRepository : CrudRepository<TeamEntryFeeInvoice, Long> {
    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): TeamEntryFeeInvoice?
}
