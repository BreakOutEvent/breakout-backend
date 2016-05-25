package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.event.Team
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.services.FeatureFlagService
import backend.services.MailService
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

        val email = Email(
                to = team.members.map { EmailAddress(it.email) },
                subject = "BreakOut 2016 - Euch wurde eine Challenge gestellt!",
                body = "Hallo Team \"${team.name}\" Euch wurde eine Challenge gestellt!<br><br>" +
                        "\"$description\", bei Erfüllung sammelt Ihr ${amount.numberStripped.toPlainString()}€ an zusätzlichen Sponsorengeldern.<br>" +
                        "Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "CHALLENGE ANNEHMEN",
                buttonUrl = "https://anmeldung.break-out.org/settings/sponsoring?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=accept_challenge",
                campaignCode = "accept_challenge"
        )

        mailService.send(email)

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

        if (challenge.hasRegisteredSponsor()) {
            val email = Email(
                    to = challenge.team!!.members.map { EmailAddress(it.email) },
                    subject = "BreakOut 2016 - Eine Challenge wurde zurückgezogen!",
                    body = "Hallo Team \"${challenge.team!!.name}\" eine Challenge wurde zurückgezogen, vielleicht wollt ihr mit dem Sponsor noch einmal darüber reden.<br>" +
                            "Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>" +
                            "Liebe Grüße<br>" +
                            "Euer BreakOut-Team",
                    campaignCode = "challenge_withdrawn"
            )

            mailService.send(email)
        }

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
        return challengeRepository.findBySponsorCoreId(userId)
    }
}
