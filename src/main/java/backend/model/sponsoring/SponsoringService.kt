package backend.model.sponsoring

import backend.model.event.Event
import backend.model.event.Team
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.springframework.security.access.prepost.PreAuthorize

interface SponsoringService {

    fun createSponsoring(event: Event, sponsor: Sponsor, teams: MutableSet<Team>, amountPerKm: Money, limit: Money): Sponsoring
    fun findByTeamId(teamId: Long): Iterable<Sponsoring>
    fun findBySponsorId(sponsorId: Long): Iterable<Sponsoring>
    fun findOne(id: Long): Sponsoring?

    @PreAuthorize("#sponsoring.isTeamMember(authentication.name)")
    fun acceptSponsoring(sponsoring: Sponsoring): Sponsoring

    @PreAuthorize("#sponsoring.isTeamMember(authentication.name)")
    fun rejectSponsoring(sponsoring: Sponsoring): Sponsoring

    @PreAuthorize("#sponsoring.checkWithdrawPermissions(authentication.name)")
    fun withdrawSponsoring(sponsoring: Sponsoring): Sponsoring

    @PreAuthorize("#unregisteredSponsor.team.isMember(authentication.name)")
    fun createSponsoringWithOfflineSponsor(event: Event, amountPerKm: Money, limit: Money, unregisteredSponsor: UnregisteredSponsor): Sponsoring

    @PreAuthorize("hasAuthority('ADMIN')")
    fun sendEmailsToSponsorsWhenEventHasStarted()

    fun sendEmailsToSponsorsWhenEventHasEnded()

    fun findAllRegisteredSponsorsWithSponsoringAtEvent(eventId: Long): Iterable<Sponsor>

    fun findAllUnregisteredSponsorsWithSponsoringAtEvent(eventId: Long): Iterable<UnregisteredSponsor>
}

