package backend.model.payment

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SponsoringInvoiceRepository : CrudRepository<SponsoringInvoice, Long> {

    fun findByTeamId(teamId: Long): Iterable<SponsoringInvoice>

    fun findByEventId(eventId:Long): Iterable<SponsoringInvoice>

    @Query("from SponsoringInvoice where initialVersionSent = false and event.id = :eventId")
    fun findByEventIdWhereInitialVersionSentIsFalse(@Param("eventId") eventId: Long): Iterable<SponsoringInvoice>

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

}
