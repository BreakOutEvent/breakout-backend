package backend.model.challenges

import backend.model.event.Team
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.transaction.Transactional

interface ChallengeService {

    @PreAuthorize("#sponsor.email.equals(authentication.name)")
    fun proposeChallenge(sponsor: Sponsor, team: Team, amount: Money, description: String): Challenge

    @PreAuthorize("#team.isMember(authentication.name)")
    fun proposeChallenge(unregisteredSponsor: UnregisteredSponsor, team: Team, amount: Money, description: String): Challenge
}

@Service
class ChallengeServiceImpl : ChallengeService {

    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    constructor(challengeRepository: ChallengeRepository) {
        this.challengeRepository = challengeRepository
    }

    @Transactional
    override fun proposeChallenge(sponsor: Sponsor, team: Team, amount: Money, description: String): Challenge {
        val challenge = Challenge(sponsor, team, amount, description)
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun proposeChallenge(unregisteredSponsor: UnregisteredSponsor, team: Team, amount: Money, description: String): Challenge {
        val challenge = Challenge(unregisteredSponsor, team, amount, description)
        return challengeRepository.save(challenge)
    }

}

interface ChallengeRepository : CrudRepository<Challenge, Long> {

}
