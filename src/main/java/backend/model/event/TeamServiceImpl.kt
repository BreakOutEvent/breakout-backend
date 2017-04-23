package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.services.mail.MailService
import backend.util.data.DonateSums
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class TeamServiceImpl(teamRepository: TeamRepository,
                      private val userService: UserService,
                      private val mailService: MailService,
                      private val configurationService: ConfigurationService) : TeamService {

    private val repository: TeamRepository = teamRepository
    private val logger: Logger

    @Transactional
    override fun create(creator: Participant, name: String, description: String, event: Event): Team {
        val team = Team(creator, name, description, event)
        // TODO: Maybe use sensible cascading?
        val savedTeam = this.save(team)
        savedTeam.invoice?.generatePurposeOfTransfer()

        userService.save(creator)
        return savedTeam
    }


    override fun invite(emailAddress: EmailAddress, team: Team) {
        team.invite(emailAddress)
        mailService.sendInvitationEmail(emailAddress, team)
        this.save(team)
    }

    private fun getInvitationUrl(token: String): String {
        val baseUrl = configurationService.getRequired("org.breakout.team.invitationurl")
        return "${baseUrl.replace("CUSTOMTOKEN", token)}?utm_source=backend&utm_medium=email&utm_campaign=invite"
    }

    override fun save(team: Team): Team = repository.save(team)

    override fun findOne(id: Long) = repository.findById(id)

    override fun findPostingsById(teamId: Long, page: Int, size: Int) = repository.findPostingsByTeamId(teamId, PageRequest(page, size))

    override fun findLocationPostingsById(id: Long) = repository.findLocationByTeamId(id)

    override fun findInvitationsForUser(user: User): List<Invitation> {
        return repository.findInvitationsWithEmail(user.email)
    }

    override fun findInvitationsForUserAndEvent(user: User, eventId: Long): List<Invitation> {
        return repository.findInvitationsWithEmailAndEventId(user.email, eventId)
    }

    override fun getDistanceForTeam(teamId: Long): Double {
        return this.findOne(teamId)?.getCurrentDistance() ?: 0.0
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

        if (team.isFull()) {
            mailService.sendTeamIsCompleteEmail(team.members.toList())
        }
    }

    override fun getDistance(teamId: Long): Double {
        return this.findOne(teamId)?.getCurrentDistance() ?: 0.0
    }

    override fun findByEventId(eventId: Long): List<Team> {
        return repository.findByEventId(eventId)
    }

    fun getSponsoringSum(team: Team): BigDecimal {
        return team.raisedAmountFromSponsorings().numberStripped
    }

    fun getChallengeSum(team: Team): BigDecimal {
        return team.raisedAmountFromChallenges().numberStripped
    }

    override fun getDonateSum(team: Team): DonateSums {
        val sponsorSum = getSponsoringSum(team)
        val challengesSum = getChallengeSum(team)

        return DonateSums(sponsorSum, challengesSum, sponsorSum + challengesSum)
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

    override fun findAllTeamSummaryProjections(): Iterable<TeamSummaryProjection> {
        return repository.findAllTeamSummaryProjections()
    }

    init {
        this.logger = LoggerFactory.getLogger(TeamServiceImpl::class.java)
    }
}
