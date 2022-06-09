package backend.model.payment

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface SponsoringInvoiceRepository : CrudRepository<SponsoringInvoice, Long> {

    @Query("""
        from SponsoringInvoice
        where (registeredSponsor is not null and registeredSponsor.supporterType is not null) or unregisteredSponsor is not null
    """)
    override fun findAll(): Iterable<SponsoringInvoice>

    @Query("from SponsoringInvoice where registeredSponsor.id = :registeredSponsorId")
    fun findBySponsorId(@Param("registeredSponsorId") registeredSponsorId: Long): Iterable<SponsoringInvoice>

    @Query("""
        select distinct i
        from SponsoringInvoice i
        left join i.challenges c on c.team.id = :teamId
        left join i.sponsorings s
        where c.id is not null
        and exists (SELECT 1 FROM Sponsoring sp JOIN sp.teams t WHERE sp.id = s.id AND t.id = :teamId)
    """)
    fun findByTeamId(@Param("teamId") teamId: Long): Iterable<SponsoringInvoice>

    fun findAllByEventId(eventId: Long): Iterable<SponsoringInvoice>

    @Query("from SponsoringInvoice where initialVersionSent = false and event.id = :eventId")
    fun findByEventIdWhereInitialVersionSentIsFalse(@Param("eventId") eventId: Long): Iterable<SponsoringInvoice>

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): SponsoringInvoice?

}
