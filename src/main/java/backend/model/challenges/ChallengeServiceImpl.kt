package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.event.Event
import backend.model.event.Team
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.services.FeatureFlagService
import backend.services.NotificationService
import backend.services.mail.MailService
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class ChallengeServiceImpl @Autowired constructor(
        private val challengeRepository: ChallengeRepository,
        private val notificationService: NotificationService,
        private val mailService: MailService,
        private val featureFlagService: FeatureFlagService) : ChallengeService {

    override fun findAllRegisteredSponsorsWithChallengesAtEvent(eventId: Long): Iterable<Sponsor> {
        return this.challengeRepository.findAllRegisteredSponsorsWithChallengesAtEvent(eventId)
    }

    override fun findAllUnregisteredSponsorsWithChallengesAtEvent(eventId: Long): Iterable<UnregisteredSponsor> {
        return this.challengeRepository.findAllUnregisteredSponsorsWithChallengesAtEvent(eventId)
    }

    override fun findAllChallengesForEvents(currentEvents: List<Event>): Iterable<Challenge> {
        return this.challengeRepository.findAllChallengesForEvents(currentEvents.map { it.id!! })
    }


    @Transactional
    override fun reject(challenge: Challenge): Challenge {
        challenge.reject()
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun addProofAsAdmin(challenge: Challenge, proof: Posting): Challenge {
        proof.challenge = challenge.id
        challenge.addProof()
        notificationService.notifyChallengeCompleted(challenge, proof)
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun addProof(challenge: Challenge, proof: Posting): Challenge {
        if (featureFlagService.isEnabled("challenge.addProof")) {
            proof.challenge = challenge.id
            challenge.addProof()
            notificationService.notifyChallengeCompleted(challenge, proof)
            return challengeRepository.save(challenge)
        } else {
            throw DomainException("Can't add proof to challenge. Feature disabled")
        }
    }

    override fun takeBack(challenge: Challenge): Challenge {
        challenge.takeBack()
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun proposeChallenge(sponsor: Sponsor, team: Team, amount: Money, description: String, maximumCount: Int?): Challenge {
        val challenge = Challenge(sponsor, team, amount, description, maximumCount)
        val users = team.members.map { it.account }.filter { !it.isBlocking(sponsor.account) }
        notificationService.notifyNewChallenge(challenge, users)
        mailService.sendChallengeWasCreatedEmail(challenge)
        return challengeRepository.save(challenge)
    }

    @Transactional
    override fun proposeChallenge(unregisteredSponsor: UnregisteredSponsor, team: Team, amount: Money, description: String, maximumCount: Int?): Challenge {
        val challenge = Challenge(unregisteredSponsor, team, amount, description, maximumCount)
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

    @Transactional
    override fun findChallengeProveProjectionById(challengeId: Long): ChallengeProofProjection {
        return challengeRepository.findChallengeProveProjectionById(challengeId)
    }
}
