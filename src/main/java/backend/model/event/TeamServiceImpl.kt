package backend.model.event

import backend.exceptions.DomainException
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.posting.Posting
import backend.model.user.Participant
import backend.model.user.UserService
import backend.services.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TeamServiceImpl : TeamService {

    private val repository: TeamRepository
    private val userService: UserService
    private val mailService: MailService

    @Autowired
    constructor(teamRepository: TeamRepository, userService: UserService, mailService: MailService) {
        this.repository = teamRepository
        this.userService = userService
        this.mailService = mailService
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
        val email = Email(
                to = listOf(emailAddress),
                subject = "${team.members.first().email} hat dich eingeladen, ein Teil seines Breakout-Teams zu werden",
                body = "Melde dich jetzt an auf www.break-out.org"
        )

        mailService.send(email)
        team.invite(emailAddress)
        this.save(team)
    }

    override fun save(team: Team) = repository.save(team)

    override fun getByID(id: Long): Team? = repository.findById(id)

    override fun findPostingsById(id: Long): List<Posting>? = repository.findPostingsById(id)

    override fun findLocationPostingsById(id: Long): List<Posting>? = repository.findLocationPostingsById(id)
}
