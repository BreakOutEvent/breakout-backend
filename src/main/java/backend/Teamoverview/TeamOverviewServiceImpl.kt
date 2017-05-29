package backend.Teamoverview

import backend.controller.exceptions.NotFoundException
import backend.model.event.Event
import backend.model.event.Team
import backend.model.event.TeamChangedEvent
import backend.model.event.TeamCreatedEvent
import backend.model.location.LocationUploadedEvent
import backend.model.posting.PostingCreatedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.NonUniqueResultException

@Service
class TeamOverviewServiceImpl(private val teamOverviewRepository: TeamoverviewRepository) : TeamOverviewService {

    private val logger: Logger = LoggerFactory.getLogger(TeamOverviewServiceImpl::class.java)

    override fun findAll(): Iterable<TeamOverview> = teamOverviewRepository.findAll()

    override fun findByTeamId(teamId: Long): TeamOverview? = teamOverviewRepository.findByTeamId(teamId)

    @Transactional
    override fun addComment(teamId: Long, comment: String) {
        val teamOverview = this.findByTeamId(teamId)
                ?: throw NotFoundException("Team with id $teamId not found in TeamOverview")

        val previousComment = teamOverview.lastContactWithHeadquarters?.comment ?: ""
        val newComment = previousComment + "----" + comment
        teamOverview.setLastContactWithHeadquarters(newComment, LocalDateTime.now())
    }

    //@EventListener
    fun onTeamCreated(teamCreatedEvent: TeamCreatedEvent) {
        val overview = TeamOverview(teamCreatedEvent.team, teamCreatedEvent.team.event)
        teamOverviewRepository.save(overview)
    }

    //@EventListener
    fun onTeamChanged(teamChangedEvent: TeamChangedEvent) {
        val team = teamChangedEvent.team
        val overview = teamOverviewRepository.findByTeamId(team.id!!) ?: createOverviewForTeam(team)
        overview.setOrUpdateValues(team.event, team)
        teamOverviewRepository.save(overview)
    }

    //@EventListener
    fun onLocationUploaded(locationUploadedEvent: LocationUploadedEvent) {
        try {
            val team = locationUploadedEvent.team
            val overview = teamOverviewRepository.findByTeamId(team.id!!) ?: createOverviewForTeam(team)
            overview.lastLocation = LastLocation(locationUploadedEvent.location)
            teamOverviewRepository.save(overview)
        } catch (e: NonUniqueResultException) {
            logger.error("Failed to update TeamOverview" +
                    e.message)
        }
    }

    //@EventListener
    fun onPostingCreated(postingCreatedEvent: PostingCreatedEvent) {
        val posting = postingCreatedEvent.posting
        val team = posting.team ?: run {
            logger.warn("Posting ${posting.id} was ignored because it has no team")
            return
        }

        val overview = teamOverviewRepository.findByTeamId(team.id!!) ?: createOverviewForTeam(team)
        overview.lastPosting = LastPosting(posting.id!!, posting.createdAt!!)
        teamOverviewRepository.save(overview)
    }

    private fun createOverviewForTeam(team: Team): TeamOverview {
        return TeamOverview(team, team.event)
    }
}

// TODO: Fire those where appropriate
class EventChangedEvent(val event: Event)
