package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.EventRepository
import backend.model.event.TeamRepository
import backend.model.event.TeamService
import backend.model.misc.Coord
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.utils.distanceCoordsListKM
import backend.view.TeamView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

@RestController
@RequestMapping("/event/{eventId}/team")
open class TeamController {

    open var teamService: TeamService
    open var eventRepository: EventRepository
    open var teamRepository: TeamRepository

    @Autowired
    constructor(teamService: TeamService, eventRepository: EventRepository, teamRepository: TeamRepository) {
        this.teamService = teamService
        this.eventRepository = eventRepository
        this.teamRepository = teamRepository
    }

    @ResponseStatus(CREATED)
    @RequestMapping("/", method = arrayOf(POST))
    fun createTeam(@PathVariable eventId: Long,
                   @AuthenticationPrincipal user: CustomUserDetails,
                   @RequestBody body: TeamView): TeamView {

        val event = eventRepository.findById(eventId) ?: throw NotFoundException("No event with id $eventId")
        val creator = user.getRole(Participant::class) ?: throw UnauthorizedException("User is no participant")

        return TeamView(teamService.create(creator, body.name!!, body.description!!, event))
    }

    @ResponseStatus(CREATED)
    @RequestMapping("/{teamId}/invitation/", method = arrayOf(POST))
    @PreAuthorize("isAuthenticated()")
    fun inviteUser(@PathVariable eventId: Long,
                   @PathVariable teamId: Long,
                   @Valid @RequestBody body: Map<String, Any>) {

        if (eventRepository.exists(eventId) == false) throw NotFoundException("No event with id $eventId")

        val team = teamRepository.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")
        val emailString = body["email"] as? String ?: throw BadRequestException("body is missing field email")
        val email = EmailAddress(emailString)
        teamService.invite(email, team)
    }

    @ResponseStatus(CREATED)
    @RequestMapping("/{teamId}/member/", method = arrayOf(POST))
    fun joinTeam(@PathVariable eventId: Long,
                 @PathVariable teamId: Long,
                 @AuthenticationPrincipal user: CustomUserDetails,
                 @Valid @RequestBody body: Map<String, String>) {

        if (eventRepository.exists(eventId) == false) throw NotFoundException("No event with id $eventId")

        val team = teamRepository.findOne(teamId) ?: throw NotFoundException("No team with id $teamId")
        val emailString = body["email"] ?: throw BadRequestException("body is missing field email")
        val email = EmailAddress(emailString)

        if (user.email != email.toString()) throw BadRequestException("Authorized user and email from request body don't match")
        val participant = user.getRole(Participant::class) ?: throw RuntimeException("User is no participant")

        // TODO: Handle Exceptions which may occur when user is not invited to team, etc.
        team.join(participant)
    }

    @RequestMapping(
            value = "/{id}/",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showTeam(@PathVariable("id") id: Long): TeamView {
        val team = teamService.getByID(id) ?: throw NotFoundException("team with id $id does not exist")
        return TeamView(team)
    }

    @RequestMapping(
            value = "/{id}/posting/",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getTeamPostings(@PathVariable("id") id: Long): List<Long> {
        val postings = teamService.findPostingsById(id) ?: throw NotFoundException("team with id $id does not exist")
        return postings.map { it.id!! }
    }

    @RequestMapping(
            value = "/{id}/distance/",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getTeamDistance(@PathVariable("id") id: Long): Double {
        val postings = teamService.findLocationPostingsById(id) ?: throw NotFoundException("team with id $id does not exist")
        val distance = distanceCoordsListKM(postings.map { it.postLocation!! })
        return distance
    }
}
