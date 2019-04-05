package backend.Teamoverview

import backend.model.event.*
import backend.model.user.Admin
import backend.model.user.UserAccount
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamOverviewServiceImpl(
        private val teamRepository: TeamRepository,
        private val contactWithHeadquartersRepository: ContactWithHeadquartersRepository
) : TeamOverviewService {

    override fun findAll(): Iterable<TeamOverview> =
            teamRepository.findAllByEventIsCurrentTrueAndHasStartedTrue()

    override fun allCalls(teamId: Long): Iterable<ContactWithHeadquarters> =
            contactWithHeadquartersRepository.findAllByTeamId(teamId)

    @Transactional
    override fun addComment(teamId: Long, comment: String, admin: UserAccount) {
        val team = teamRepository.findById(teamId) ?: return
        val contact = ContactWithHeadquarters(team, comment, admin)
        contactWithHeadquartersRepository.save(contact)
    }
}
