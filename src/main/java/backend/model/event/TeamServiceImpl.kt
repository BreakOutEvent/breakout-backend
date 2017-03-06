package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.model.challenges.ChallengeStatus
import backend.model.location.Location
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.sponsoring.SponsoringStatus.ACCEPTED
import backend.model.sponsoring.SponsoringStatus.PAYED
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.services.MailService
import backend.util.data.ChallengeDonateSums
import backend.util.data.DonateSums
import backend.util.parallelStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
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

    @Transactional
    override fun create(creator: Participant, name: String, description: String, event: Event): Team {
        val team = Team(creator, name, description, event)
        // TODO: Maybe use sensible cascading?
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

    override fun save(team: Team): Team = repository.save(team)

    override fun findOne(id: Long) = repository.findById(id)

    override fun findPostingsById(id: Long) = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long) = repository.findLocationByTeamId(id)

    override fun getLocationMaxDistanceById(id: Long): Location? {
        return repository.getLocationMaxDistanceById(id, PageRequest(0, 1)).firstOrNull()
    }

    override fun findInvitationsForUser(user: User): List<Invitation> {
        return repository.findInvitationsWithEmail(user.email)
    }

    override fun findInvitationsForUserAndEvent(user: User, eventId: Long): List<Invitation> {
        return repository.findInvitationsWithEmailAndEventId(user.email, eventId)
    }

    override fun getLinearDistanceForTeam(teamId: Long): Double {
        val locationDistance = this.getLocationMaxDistanceById(teamId)
        return locationDistance?.distance ?: 0.0
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

    override fun getDistance(teamId: Long): Double {
        return this.getLinearDistanceForTeam(teamId)
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
        val distance = this.getLinearDistanceForTeam(team.id!!)

        return team.sponsoring.parallelStream().filter { it.status == ACCEPTED || it.status == PAYED }.map { sponsoring ->
            val amount = sponsoring.amountPerKm.numberStripped * BigDecimal.valueOf(distance)
            return@map when (amount > sponsoring.limit.numberStripped) {
                true -> sponsoring.limit.numberStripped
                false -> amount
            }.setScale(2, BigDecimal.ROUND_HALF_UP)
        }.reduce { acc: BigDecimal, sponsorSum: BigDecimal ->
            acc + sponsorSum
        }.orElseGet { BigDecimal.ZERO }
    }

    fun getChallengeSum(team: Team): ChallengeDonateSums {
        return team.challenges.parallelStream().map { challenge ->
            return@map when (challenge.status) {
                ChallengeStatus.WITH_PROOF -> ChallengeDonateSums(challenge.amount.numberStripped, BigDecimal.ZERO)
                ChallengeStatus.PROOF_ACCEPTED -> ChallengeDonateSums(BigDecimal.ZERO, challenge.amount.numberStripped)
                else -> ChallengeDonateSums(BigDecimal.ZERO, BigDecimal.ZERO)
            }
        }.reduce { accSums: ChallengeDonateSums, challengeDonateSums: ChallengeDonateSums ->
            ChallengeDonateSums(accSums.withProofSum + challengeDonateSums.withProofSum,
                    accSums.acceptedProofSum + challengeDonateSums.acceptedProofSum)
        }.orElseGet { ChallengeDonateSums(BigDecimal.ZERO, BigDecimal.ZERO) }
    }

    override fun getDonateSum(team: Team): DonateSums {
        val sponsorSum = getSponsoringSum(team)
        val challengesSum = getChallengeSum(team)

        val fullSum = sponsorSum + challengesSum.withProofSum + challengesSum.acceptedProofSum

        return DonateSums(sponsorSum,
                challengesSum.withProofSum,
                challengesSum.acceptedProofSum,
                fullSum)
    }

    @Transactional
    override fun getDonateSum(teamId: Long): DonateSums {
        val team: Team = this.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        return getDonateSum(team)
    }

    override fun searchByString(search: String): List<Team> {
        return repository.searchByString(search)
    }

    override fun sendEmailsToTeamsWhenEventHasEnded() {

        repository.findAll().filter { it.hasStarted }
                .apply { logger.info("Sending emails that event has ended to ${this.count()} teams") }
                .forEach {
                    val mail = Email(
                            to = it.members.map { EmailAddress(it.email) },
                            subject = "BreakOut 2016 - War ein voller Erfolg!",
                            body = getEmailBodyToTeamsWhenEventHasEnded(it),
                            buttonText = "ZUM LIVEBLOG",
                            buttonUrl = "https://event.break-out.org/?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=event_ended_team")

                    mailService.sendAsync(mail)
                }
    }

    private fun getEmailBodyToTeamsWhenEventHasEnded(team: Team): String {

        return "Liebes Team ${team.name}," +

                "Ihr habt es geschafft und habt erfolgreich bei BreakOut 2016 teilgenommen. Wir feiern Euch hart ab. Mega, dass Ihr mitgemacht und so gemeinsam Spenden für das DAFI-Programm der UNO Flüchtlingshilfe gesammelt habt.<br><br>" +

                "Damit wir Eure überragende Leistung gebührend zusammen feiern können, seid Ihr alle herzlich zur BreakOut- Siegerehrung eingeladen.<br>" +
                "Diese findet am 15. Juni um 18:00 Uhr in der 089-Bar in München statt.<br><br>" +

                "Weitere Informationen zur Siegerehrung und Eurer Reise folgen bald.<br><br>" +

                "Genießt den Abend. Wir wünschen Euch eine schöne und sichere Heimreise.<br>" +
                "Ihr BreakOut-Team"
    }

    override fun findAll(): Iterable<Team> {
        return repository.findAll()
    }
}
