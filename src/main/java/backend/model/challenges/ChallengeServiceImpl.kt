package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.event.Team
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.services.FeatureFlagService
import backend.services.mail.MailService
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class ChallengeServiceImpl : ChallengeService {

    private val challengeRepository: ChallengeRepository
    private val mailService: MailService
    private val featureFlagService: FeatureFlagService


    @Autowired
    constructor(challengeRepository: ChallengeRepository,
                mailService: MailService,
                featureFlagService: FeatureFlagService) {

        this.challengeRepository = challengeRepository
        this.mailService = mailService
        this.featureFlagService = featureFlagService
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
        if (featureFlagService.isEnabled("challenge.addProof")) {
            proof.challenge = challenge
            challenge.addProof(proof)
            return challengeRepository.save(challenge)
        } else {
            throw DomainException("Can't add proof to challenge. Feature disabled")
        }
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
        mailService.sendChallengeWasCreatedEmail(challenge)
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun proposeChallenge(unregisteredSponsor: UnregisteredSponsor, team: Team, amount: Money, description: String): Challenge {
        val challenge = Challenge(unregisteredSponsor, team, amount, description)
        return challengeRepository.save(challenge)
    }


    @Transactional
    override fun withdraw(challenge: Challenge): Challenge {
        challenge.withdraw()
        mailService.sendChallengeWasWithdrawnEmail(challenge)
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun findOne(challengeId: Long): Challenge? {
        return challengeRepository.findOne(challengeId)
    }

    @Transactional
    override fun findByTeamId(teamId: Long): Iterable<Challenge> {
        return challengeRepository.findByTeamId(teamId)
    }

    @Transactional
    override fun findBySponsorId(userId: Long): Iterable<Challenge> {
        return challengeRepository.findBySponsorAccountId(userId)
    }
}
