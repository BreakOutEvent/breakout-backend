package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.model.location.Location
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.services.MailService
import backend.util.distanceCoordsListKMfromStart
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamServiceImpl : TeamService {

    private val repository: TeamRepository
    private val userService: UserService
    private val mailService: MailService
    private val configurationService: ConfigurationService

    @Autowired
    constructor(teamRepository: TeamRepository, userService: UserService, mailService: MailService, configurationService: ConfigurationService) {
        this.repository = teamRepository
        this.userService = userService
        this.mailService = mailService
        this.configurationService = configurationService
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
                subject = "Du wurdest zur Teilnahme an BreakOut 2016 eingeladen!",
                body = "${team.members.first().firstname} ${team.members.first().lastname} möchte mit Dir ein Abenteuer bestreiten!<br><br>" +
                        "BreakOut ist ein Spendenmarathon, bei dem Geld für das DAFI-Projekt der UNO-Flüchtlingshilfe gesammelt wird. <br><br>" +
                        " In Zweierteams versucht Ihr, euch ab Startschuss binnen 36 Stunden so weit wie möglich von München zu entfernen. Dabei gilt es, für das Reisen kein Geld auszugeben – vielmehr sammelt Ihr pro zurückgelegtem Kilometer Geld für das DAFI-Programm der UNO-Flüchtlingshilfe.<br>" +
                        "Das Konzept folgt damit der Idee eines Spendenmarathons: Im Vorfeld akquiriert Ihr eigene Sponsoren, die dann pro gereistem Kilometer einen vorab festgelegten Betrag an die UNO-Flüchtlingshilfe spenden.<br><br>" +
                        "Wenn Du Lust hast bei BreakOut teilzunehmen, klicke auf den Button am Ende der E-Mail.<br><br>" +
                        "Du hast Fragen oder benötgist Unterstützung? Schreib uns eine E-Mail an event@break-out.org.<br><br>" +
                        "Beste Grüße,<br>" +
                        "Dein BreakOut-Team",
                buttonText = "EINLADUNG ANNEHMEN",
                buttonUrl = getInvitationUrl(invitation.invitationToken)
        )

        mailService.send(email)
        this.save(team)
    }

    private fun getInvitationUrl(token: String): String {
        val baseUrl = configurationService.getRequired("org.breakout.team.invitationurl")
        return baseUrl.replace("CUSTOMTOKEN", token)
    }

    override fun save(team: Team) = repository.save(team)

    override fun findOne(id: Long) = repository.findById(id)

    override fun findPostingsById(id: Long) = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long) = repository.findLocationPostingsById(id)

    override fun getLocationMaxDistanceById(id: Long): Location? {
        val postingList = repository.getLocationMaxDistanceById(id)
        if (postingList.size <= 0) {
            return null
        } else {
            return postingList.first()
        }
    }

    override fun findInvitationsForUser(user: User): List<Invitation> {
        return repository.findInvitationsWithEmail(user.email)
    }

    override fun findInvitationsForUserAndEvent(user: User, eventId: Long): List<Invitation> {
        return repository.findInvitationsWithEmailAndEventId(user.email, eventId)
    }

    override fun getLinearDistanceForTeamFromPostings(teamId: Long): Double {
        val postingDistance = this.getLocationMaxDistanceById(teamId)
        var distance = postingDistance?.distance ?: 0.0

        return distance
    }

    override fun getActualDistanceForTeamFromPostings(teamId: Long): Double {

        val team: Team = this.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val startingCoordinates = team.event.startingLocation
        val postingCoordinates = this.findLocationPostingsById(teamId).map { it.coord }
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

    override fun getDistance(teamId: Long): Map<String, Double> {
        val linearDistance = this.getLinearDistanceForTeamFromPostings(teamId)
        val actualDistance = this.getActualDistanceForTeamFromPostings(teamId)

        return mapOf("actual_distance" to actualDistance, "linear_distance" to linearDistance)
    }

    private fun getFullTeamMailForMember(participants: Set<Participant>): List<Email> {
        val first = participants.first()
        val second = participants.last()

        val toFirst = Email(to = listOf(EmailAddress(first.email)),
                subject = "BreakOut 2016 - Dein Team ist vollständig, bitte zahle die Startgebühr",
                body = "Hallo ${first.firstname},<br>" +
                        "Herzlichen Glückwunsch Du bist jetzt mit ${second.firstname} in einem Team und Euer Team ist damit vollständig." +
                        "Um endgültig angemeldet zu sein müsst Ihr jetzt nur noch die Teilnahmegebühr von 30€ pro Person bis spätestens 18. Mai an folgendes Konto überweisen:<br><br>" +
                        "Inhaber: 	Daria Brauner<br>" +
                        "IBAN: 		DE60 7002 2200 0072 7083 26<br>" +
                        "BIC: 		FDDODEMMXXX<br>" +
                        "Zweck:		${getBankingSubject(first)}<br><br>" +
                        "Davon sind 10€ Deposit, die Du zurück bekommst, wenn Dein Team mehr als 100€ Spenden eingenommen hat.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team<br>"
        )

        val toSecond = Email(to = listOf(EmailAddress(second.email)),
                subject = "BreakOut 2016 - Dein Team ist vollständig, bitte zahle die Startgebühr",
                body = "Hallo ${second.firstname},<br>" +
                        "Herzlichen Glückwunsch Du bist jetzt mit ${first.firstname} in einem Team und Euer Team ist damit vollständig." +
                        "Um endgültig angemeldet zu sein müsst Ihr jetzt nur noch die Teilnahmegebühr von 30€ pro Person bis spätestens 18. Mai an folgendes Konto überweisen:<br><br>" +
                        "Inhaber: 	Daria Brauner<br>" +
                        "IBAN: 		DE60 7002 2200 0072 7083 26<br>" +
                        "BIC: 		FDDODEMMXXX<br>" +
                        "Zweck:		${getBankingSubject(second)}<br><br>" +
                        "Davon sind 10€ Deposit, die Du zurück bekommst, wenn Dein Team mehr als 100€ Spenden eingenommen hat.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team<br>"
        )

        return listOf(toFirst, toSecond)
    }

    private fun getBankingSubject(participant: Participant): String {
        var subject: String = "${participant.currentTeam!!.id}-BO16-${participant.firstname}-${participant.lastname}"
        subject = subject.replace("ä", "ae").replace("ü", "ue").replace("ö", "oe").replace("ß", "ss");
        if (subject.length > 140) {
            return subject.substring(0, 140)
        } else {
            return subject
        }
    }
}
