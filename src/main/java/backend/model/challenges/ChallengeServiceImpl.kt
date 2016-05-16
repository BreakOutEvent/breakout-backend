package backend.model.challenges

import backend.model.event.Team
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class ChallengeServiceImpl : ChallengeService {

    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    constructor(challengeRepository: ChallengeRepository) {
        this.challengeRepository = challengeRepository
    }

    @Transactional
    override fun accept(challenge: Challenge): Challenge {
        challenge.accept()
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun reject(challenge: Challenge): Challenge {
        challenge.reject()
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun addProof(challenge: Challenge, proof: Posting): Challenge {
        challenge.addProof(proof)
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun acceptProof(challenge: Challenge): Challenge {
        challenge.accept()
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun rejectProof(challenge: Challenge): Challenge {
        challenge.rejectProof()
        return challengeRepository.save(challenge)
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

    @Transactional
    override fun findOne(challengeId: Long): Challenge? {
        return challengeRepository.findOne(challengeId)
    }

}
