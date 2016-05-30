package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.model.challenges.ChallengeStatus
import backend.model.location.Location
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.services.MailService
import backend.util.distanceCoordsListKMfromStart
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class TeamServiceImpl : TeamService {

    private val repository: TeamRepository
    private val userService: UserService
    private val mailService: MailService
    private val configurationService: ConfigurationService
    private val logger: Logger

    @Autowired
    constructor(teamRepository: TeamRepository, userService: UserService, mailService: MailService, configurationService: ConfigurationService) {
        this.repository = teamRepository
        this.userService = userService
        this.mailService = mailService
        this.configurationService = configurationService
        this.logger = LoggerFactory.getLogger(TeamServiceImpl::class.java)
    }

    // TODO: Maybe make this transactional
    override fun create(creator: Participant, name: String, description: String, event: Event): Team {
        if (creator.currentTeam != null) throw DomainException("participant ${creator.core.id} already is part of a team")
        val team = Team(creator, name, description, event)
        val savedTeam = this.save(team)
        userService.save(creator)
        return savedTeam
    }

    override fun invite(emailAddress: EmailAddress, team: Team) {

        val invitation = team.invite(emailAddress)
        val email = Email(
                to = listOf(emailAddress),
                subject = "BreakOut 2016 - Du wurdest zur Teilnahme eingeladen!",
                body = "${team.members.first().firstname} ${team.members.first().lastname} möchte mit Dir ein Abenteuer bestreiten!<br><br>" +
                        "BreakOut ist ein Spendenmarathon, bei dem Geld für das DAFI-Projekt der UNO-Flüchtlingshilfe gesammelt wird.<br><br>" +
                        "In Zweierteams versucht Ihr, euch ab Startschuss binnen 36 Stunden so weit wie möglich von München zu entfernen. Dabei gilt es, für das Reisen kein Geld auszugeben – vielmehr sammelt Ihr pro zurückgelegtem Kilometer Geld für das DAFI-Programm der UNO-Flüchtlingshilfe.<br>" +
                        "Das Konzept folgt damit der Idee eines Spendenmarathons: Im Vorfeld akquiriert Ihr eigene Sponsoren, die dann pro gereistem Kilometer einen vorab festgelegten Betrag an die UNO-Flüchtlingshilfe spenden.<br><br>" +
                        "Wenn Du Lust hast bei BreakOut teilzunehmen, klicke auf den Button am Ende der E-Mail.<br><br>" +
                        "Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an <a href=\"event@break-out.org\">event@break-out.org</a>.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "EINLADUNG ANNEHMEN",
                buttonUrl = getInvitationUrl(invitation.invitationToken),
                campaignCode = "invite"
        )

        mailService.send(email)
        this.save(team)
    }

    private fun getInvitationUrl(token: String): String {
        val baseUrl = configurationService.getRequired("org.breakout.team.invitationurl")
        return "${baseUrl.replace("CUSTOMTOKEN", token)}?utm_source=backend&utm_medium=email&utm_campaign=invite"
    }

    override fun save(team: Team) = repository.save(team)

    override fun findOne(id: Long) = repository.findById(id)

    override fun findPostingsById(id: Long) = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long) = repository.findLocationByTeamId(id)

    override fun getLocationMaxDistanceById(id: Long): Location? {
        val locationList = repository.getLocationMaxDistanceById(id)
        if (locationList.size <= 0) {
            return null
        } else {
            return locationList.dropWhile { !it.isDuringEvent() }.firstOrNull()
        }
    }

    override fun findInvitationsForUser(user: User): List<Invitation> {
        return repository.findInvitationsWithEmail(user.email)
    }

    override fun findInvitationsForUserAndEvent(user: User, eventId: Long): List<Invitation> {
        return repository.findInvitationsWithEmailAndEventId(user.email, eventId)
    }

    override fun getLinearDistanceForTeam(teamId: Long): Double {
        val locationDistance = this.getLocationMaxDistanceById(teamId)
        val distance = locationDistance?.distance ?: 0.0

        return distance
    }

    override fun getActualDistanceForTeam(teamId: Long): Double {

        val team: Team = this.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val startingCoordinates = team.event.startingLocation
        val postingCoordinates = this.findLocationPostingsById(teamId).dropWhile { !it.isDuringEvent() }.map { it.coord }
        val distance = distanceCoordsListKMfromStart(startingCoordinates, postingCoordinates)

        return distance
    }

    override fun findInvitationsByInviteCode(code: String): Invitation? {
        return repository.findInvitationsByInviteCode(code)
    }

    @Transactional
    override fun leave(team: Team, participant: Participant) {
        team.leave(participant)
    }

    @Transactional
    override fun join(participant: Participant, team: Team) {
        val members = team.join(participant)
        if (members.size == 2) {
            val emails = getFullTeamMailForMember(members)
            emails.forEach { email ->
                mailService.send(email)
            }
        }
    }

    @Cacheable(cacheNames = arrayOf("singleCache"), key = "'functionDistanceTeam'.concat(#teamId)")
    override fun getDistance(teamId: Long): Map<String, Double> {
        val linearDistance = this.getLinearDistanceForTeam(teamId)
        val actualDistance = this.getActualDistanceForTeam(teamId)

        return mapOf("actual_distance" to actualDistance, "linear_distance" to linearDistance)
    }

    override fun getFullTeamMailForMember(participants: Set<Participant>): List<Email> {
        val first = participants.first()
        val second = participants.last()

        val toFirst = Email(to = listOf(EmailAddress(first.email)),
                subject = "BreakOut 2016 - Dein Team ist vollständig, bitte zahle die Startgebühr",
                body = "Hallo ${first.firstname},<br><br>" +
                        "Herzlichen Glückwunsch Du bist jetzt mit ${second.firstname} in einem Team und Euer Team ist damit vollständig.<br>" +
                        "Um Eure Anmeldung abzuschließen, müsst Ihr nur noch die Teilnahmegebühr von 30€ pro Person bis spätestens 18. Mai überweisen.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "JETZT ZAHLEN",
                buttonUrl = "https://anmeldung.break-out.org/payment?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=payment",
                campaignCode = "payment_initial"
        )

        val toSecond = Email(to = listOf(EmailAddress(second.email)),
                subject = "BreakOut 2016 - Dein Team ist vollständig, bitte zahle die Startgebühr",
                body = "Hallo ${second.firstname},<br><br>" +
                        "Herzlichen Glückwunsch Du bist jetzt mit ${first.firstname} in einem Team und Euer Team ist damit vollständig.<br>" +
                        "Um Eure Anmeldung abzuschließen, müsst Ihr nur noch die Teilnahmegebühr von 30€ pro Person bis spätestens 18. Mai überweisen.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "JETZT ZAHLEN",
                buttonUrl = "https://anmeldung.break-out.org/payment?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=payment",
                campaignCode = "payment_initial"
        )

        return listOf(toFirst, toSecond)
    }

    override fun findByEventId(eventId: Long): List<Team> {
        return repository.findByEventId(eventId)
    }

    fun getSponsoringSum(team: Team): BigDecimal {
        val distanceKm = this.getLinearDistanceForTeam(team.id!!)
        val sponsorSum = BigDecimal.ZERO

        team.sponsoring.forEach { sponsoring ->
            val amount = sponsoring.amountPerKm.numberStripped.multiply(BigDecimal(distanceKm))
            if (amount.compareTo(sponsoring.limit.numberStripped) == -1) {
                sponsorSum.add(sponsoring.limit.numberStripped)
            } else {
                sponsorSum.add(amount)
            }
        }

        return sponsorSum
    }

    fun getChallengeSum(team: Team): Map<String, BigDecimal> {
        val withProofSum = BigDecimal.ZERO
        val acceptedProofSum = BigDecimal.ZERO

        team.challenges.forEach { challenge ->
            if (challenge.status == ChallengeStatus.WITH_PROOF) {
                withProofSum.add(challenge.amount.numberStripped)
            }

            if (challenge.status == ChallengeStatus.PROOF_ACCEPTED) {
                acceptedProofSum.add(challenge.amount.numberStripped)
            }
        }

        return mapOf(
                "challenges_with_proof_sum" to withProofSum,
                "challenges_accepted_proof_sum" to acceptedProofSum)
    }

    @Cacheable(cacheNames = arrayOf("singleCache"), key = "'functionDonateSumTeam'.concat(#team.id)")
    override fun getDonateSum(team: Team): Map<String, BigDecimal> {
        val sponsorSum = getSponsoringSum(team)

        val challengesSum = getChallengeSum(team)

        val fullSum = BigDecimal.ZERO
        fullSum.add(sponsorSum)
        fullSum.add(challengesSum["challenges_with_proof_sum"]!!)

        return mapOf(
                "sponsoring_sum" to sponsorSum,
                "challenges_with_proof_sum" to challengesSum["challenges_with_proof_sum"]!!,
                "challenges_accepted_proof_sum" to challengesSum["challenges_accepted_proof_sum"]!!,
                "full_sum" to fullSum)
    }

    override fun getDonateSum(teamId: Long): Map<String, BigDecimal> {
        val team: Team = this.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        return getDonateSum(team)
    }
}
