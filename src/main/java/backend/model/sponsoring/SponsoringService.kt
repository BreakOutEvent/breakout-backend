package backend.model.sponsoring

import backend.model.event.Team
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.springframework.security.access.prepost.PreAuthorize

interface SponsoringService {

    fun createSponsoring(sponsor: Sponsor, team: Team, amountPerKm: Money, limit: Money): Sponsoring
    fun findByTeamId(teamId: Long): Iterable<Sponsoring>
    fun findBySponsorId(sponsorId: Long): Iterable<Sponsoring>
    fun findOne(id: Long): Sponsoring?

    @PreAuthorize("#sponsoring.team.isMember(authentication.name)")
    fun acceptSponsoring(sponsoring: Sponsoring): Sponsoring

    @PreAuthorize("#sponsoring.team.isMember(authentication.name)")
    fun rejectSponsoring(sponsoring: Sponsoring): Sponsoring

    @PreAuthorize("#sponsoring.checkWithdrawPermissions(authentication.name)")
    fun withdrawSponsoring(sponsoring: Sponsoring): Sponsoring

    @PreAuthorize("#team.isMember(authentication.name)")
    fun createSponsoringWithOfflineSponsor(team: Team, amountPerKm: Money, limit: Money, unregisteredSponsor: UnregisteredSponsor): Sponsoring

    @PreAuthorize("hasAuthority('ADMIN')")
    fun sendEmailsToSponsorsWhenEventHasStarted()

    fun sendEmailsToSponsorsWhenEventHasEnded()

    fun findAllRegisteredSponsorsWithSponsoringAtEvent(eventId: Long): Iterable<Sponsor>

    fun findAllUnregisteredSponsorsWithSponsoringAtEvent(eventId: Long): Iterable<UnregisteredSponsor>
}

