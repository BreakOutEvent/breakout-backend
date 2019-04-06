package backend.model.payment

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SponsoringInvoiceRepository : CrudRepository<SponsoringInvoice, Long> {

    @Query("from SponsoringInvoice where registeredSponsor.id = :registeredSponsorId")
    fun findBySponsorId(@Param("registeredSponsorId") registeredSponsorId: Long): Iterable<SponsoringInvoice>

    @Query("""
        select distinct i
        from SponsoringInvoice i
        left join i.sponsorings s on s.team.id = :teamId
        left join i.challenges c on c.team.id = :teamId
        where s.id is not null or c.id is not null
    """)
    fun findByTeamId(@Param("teamId") teamId: Long): Iterable<SponsoringInvoice>

    fun findAllByEventId(eventId: Long): Iterable<SponsoringInvoice>

    @Query("from SponsoringInvoice where initialVersionSent = false and event.id = :eventId")
    fun findByEventIdWhereInitialVersionSentIsFalse(@Param("eventId") eventId: Long): Iterable<SponsoringInvoice>

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

}
