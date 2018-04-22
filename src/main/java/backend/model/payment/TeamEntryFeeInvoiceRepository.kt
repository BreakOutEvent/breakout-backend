package backend.model.payment

import org.springframework.data.jpa.repository.JpaRepository

interface TeamEntryFeeInvoiceRepository : JpaRepository<TeamEntryFeeInvoice, Long> {
    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): TeamEntryFeeInvoice?
}
