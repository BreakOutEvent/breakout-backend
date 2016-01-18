package backend.model.event

import backend.model.user.Participant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TeamServiceImpl : TeamService {

    private val repository: TeamRepository

    @Autowired
    constructor(repository: TeamRepository) {
        this.repository = repository
    }

    override fun create(creator: Participant, name: String, description: String): Team {
        if (creator.currentTeam != null) throw Exception("participant ${creator.core!!.id} already is part of a team")
        val team = Team(creator = creator, name = name, description = description)
        return this.save(team)
    }

    override fun invite(email: String, team: Team) {
        // TODO: Send Email to the person to be invited
        team.invitation = Invitation(email)
        this.save(team)
    }

    override fun save(team: Team) = repository.save(team)

}
