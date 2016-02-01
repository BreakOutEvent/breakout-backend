package backend.controller

import backend.CustomUserDetails
import backend.controller.exceptions.ResourceNotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.EventRepository
import backend.model.event.TeamRepository
import backend.model.event.TeamService
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import backend.view.TeamView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

@RestController
@RequestMapping("/event/{eventId}/team")
class TeamController {

    lateinit var teamService: TeamService
    lateinit var eventRepository: EventRepository
    lateinit var teamRepository: TeamRepository

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

        val event = eventRepository.findById(eventId) ?: throw ResourceNotFoundException("No event with id $eventId")
        val creator = user.getRole(Participant::class.java) as? Participant ?:
                throw UnauthorizedException("User is no participant")

        return TeamView(teamService.create(creator, body.name!!, body.description!!, event))
    }

    @ResponseStatus(CREATED)
    @RequestMapping("/{teamId}/invitation/", method = arrayOf(POST))
    fun inviteUser(@PathVariable eventId: Long,
                   @PathVariable teamId: Long,
                   @AuthenticationPrincipal user: CustomUserDetails,
                   @Valid @RequestBody body: Map<String, Any>) {

        val event = eventRepository.findById(eventId) ?: throw ResourceNotFoundException("No event with id $eventId")
        val team = teamRepository.findOne(teamId) ?: throw ResourceNotFoundException("No team with id $teamId")

        val emailString = body.get("email") as? String ?: throw Exception("body is missing field email")
        val email = EmailAddress(emailString)
        teamService.invite(email, team)
    }

    @ResponseStatus(CREATED)
    @RequestMapping("/{teamId}/member/", method = arrayOf(POST))
    fun joinTeam(@PathVariable eventId: Long,
                 @PathVariable teamId: Long,
                 @AuthenticationPrincipal user: CustomUserDetails,
                 @Valid @RequestBody body: Map<String, String>) {

        val event = eventRepository.findOne(eventId) ?: throw ResourceNotFoundException("No event with id $eventId")
        val team = teamRepository.findOne(teamId) ?: throw ResourceNotFoundException("No team with id $teamId")
        val emailString = body.get("email") ?: throw Exception("body is missing field email")
        val email = EmailAddress(emailString)

        if (user.email != email.toString()) throw Exception("Authorized user and email from request body don't match")

        val participant = user.getRole(Participant::class.java) as? Participant ?:
                throw RuntimeException("User is no participant")

        // TODO: Handle Exceptions which may occur when user is not invited to team, etc.
        team.join(participant)
    }
}
