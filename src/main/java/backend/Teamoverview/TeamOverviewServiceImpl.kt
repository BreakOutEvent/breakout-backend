package backend.Teamoverview

import backend.model.event.Event
import backend.model.event.Team
import backend.model.event.TeamChangedEvent
import backend.model.event.TeamCreatedEvent
import backend.model.location.Location
import backend.model.location.LocationUploadedEvent
import backend.model.posting.Posting
import backend.model.posting.PostingCreatedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class TeamOverviewServiceImpl(private val teamOverviewRepository: TeamoverviewRepository) : TeamOverviewService {

    private val logger: Logger = LoggerFactory.getLogger(TeamOverviewServiceImpl::class.java)

    override fun findAll(): Iterable<TeamOverview> = teamOverviewRepository.findAll()

    @EventListener
    fun onTeamCreated(teamCreatedEvent: TeamCreatedEvent) {
        val overview = TeamOverview(teamCreatedEvent.team, teamCreatedEvent.team.event)
        teamOverviewRepository.save(overview)
    }

    @EventListener
    fun onTeamChanged(teamChangedEvent: TeamChangedEvent) {
        val team = teamChangedEvent.team
        val overview = teamOverviewRepository.findByTeamId(team.id!!) ?: createOverviewForTeam(team)
        overview.setOrUpdateValues(team.event, team)
        teamOverviewRepository.save(overview)
    }

    @EventListener
    fun onLocationUploaded(locationUploadedEvent: LocationUploadedEvent) {
        val team = locationUploadedEvent.team
        val overview = teamOverviewRepository.findByTeamId(team.id!!) ?: createOverviewForTeam(team)
        val coord = Coord(locationUploadedEvent.location.coord.latitude, locationUploadedEvent.location.coord.longitude)

        overview.lastLocation = LastLocation(coord, locationUploadedEvent.location.id!!)
        teamOverviewRepository.save(overview)
    }

    @EventListener
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
