package backend.model.event

import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TeamServiceImpl : TeamService {

    private val repository: TeamRepository
    private val event: Event = Event()

    @Autowired lateinit var userService: UserService

    @Autowired
    constructor(repository: TeamRepository) {
        this.repository = repository
    }

    override fun create(creator: Participant, name: String, description: String, event: Event): Team {
        if (creator.currentTeam != null) throw Exception("participant ${creator.core.id} already is part of a team")
        val team = Team(creator, name, description, event)
        val savedTeam = this.save(team)
        userService.save(creator)
        return savedTeam
    }

    override fun invite(email: EmailAddress, team: Team) {
        // TODO: Send Email to the person to be invited
        // TODO: What if user already exists?
        team.invite(email)
        this.save(team)
    }

    override fun save(team: Team) = repository.save(team)

}
