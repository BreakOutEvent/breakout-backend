package backend.model.payment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SponsoringInvoiceRepository : JpaRepository<SponsoringInvoice, Long> {

    fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice>

    fun findAllByEventId(eventId: Long): Iterable<SponsoringInvoice>

    @Query("from SponsoringInvoice where initialVersionSent = false and event.id = :eventId")
    fun findByEventIdWhereInitialVersionSentIsFalse(@Param("eventId") eventId: Long): Iterable<SponsoringInvoice>

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

}
