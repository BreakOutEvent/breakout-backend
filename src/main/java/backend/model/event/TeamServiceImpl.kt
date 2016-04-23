package backend.model.event

import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.services.MailService
import backend.util.distanceCoordsListKMfromStart
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

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

    override fun create(creator: Participant, name: String, description: String, event: Event): Team {
        if (creator.currentTeam != null) throw DomainException("participant ${creator.core.id} already is part of a team")
        val team = Team(creator, name, description, event)
        val savedTeam = this.save(team)
        userService.save(creator)
        return savedTeam
    }

    override fun invite(emailAddress: EmailAddress, team: Team) {

        // TODO: What if user already exists?
        // TODO: Should the creation of emails be moved to a seperate entity?
        val invitation = team.invite(emailAddress)
        val inviteUrl = getInvitationUrl(invitation.invitationToken)
        val email = Email(
                to = listOf(emailAddress),
                subject = "${team.members.first().email} hat dich eingeladen, ein Teil seines Breakout-Teams zu werden.",
                body = "Klicke folgenden Link, um ein Teil von BreakOut zu werden: $inviteUrl")

        mailService.send(email)
        this.save(team)
    }

    private fun getInvitationUrl(token: String): String {
        val baseUrl = configurationService.getRequired("org.breakout.team.invitationurl")
        return baseUrl.replace("CUSTOMTOKEN", token)
    }

    override fun save(team: Team) = repository.save(team)

    override fun findOne(id: Long): Team? = repository.findById(id)

    override fun findPostingsById(id: Long): List<Long>? = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long): List<Posting> = repository.findLocationPostingsById(id)

    override fun getPostingMaxDistanceById(id: Long): Posting? {
        val postingList = repository.getPostingMaxDistanceById(id)
        if (postingList.size <= 0) {
            return null
        } else {
            return postingList.first()
        }
    }

    override fun findInvitationsForUser(user: User): List<Invitation> {
        return repository.findInvitationsWithEmail(user.email)
    }

    override fun getLinearDistanceForTeamFromPostings(teamId: Long): Double {
        val postingDistance = this.getPostingMaxDistanceById(teamId)
        var distance = postingDistance?.distance ?: 0.0

        return distance
    }

    override fun getActualDistanceForTeamFromPostings(teamId: Long): Double {

        val team: Team = this.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val startingCoordinates = team.event.startingLocation
        val postingCoordinates = this.findLocationPostingsById(teamId).map { it.location!!.toCoord() }
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
}
