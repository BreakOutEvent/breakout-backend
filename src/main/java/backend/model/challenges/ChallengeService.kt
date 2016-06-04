package backend.model.challenges

import backend.model.event.Team
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.springframework.security.access.prepost.PreAuthorize

interface ChallengeService {

    @PreAuthorize("#sponsor.email.equals(authentication.name)")
    fun proposeChallenge(sponsor: Sponsor, team: Team, amount: Money, description: String): Challenge

    @PreAuthorize("#team.isMember(authentication.name)")
    fun proposeChallenge(unregisteredSponsor: UnregisteredSponsor, team: Team, amount: Money, description: String): Challenge

    fun findOne(challengeId: Long): Challenge?

    fun findByTeamId(teamId: Long): Iterable<Challenge>

    @PreAuthorize("#challenge.team.isMember(authentication.name)")
    fun accept(challenge: Challenge): Challenge

    @PreAuthorize("#challenge.team.isMember(authentication.name)")
    fun reject(challenge: Challenge): Challenge

    @PreAuthorize("#challenge.team.isMember(authentication.name)")
    fun addProof(challenge: Challenge, proof: Posting): Challenge

    @PreAuthorize("#challenge.team.isMember(authentication.name)")
    fun acceptProof(challenge: Challenge): Challenge

    @PreAuthorize("#challenge.team.isMember(authentication.name)")
    fun rejectProof(challenge: Challenge): Challenge

    @PreAuthorize("#challenge.checkWithdrawPermissions(authentication.name)")
    fun withdraw(challenge: Challenge): Challenge

    fun findBySponsorId(userId: Long): Iterable<Challenge>
}

