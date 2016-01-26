package backend.controller

import backend.CustomUserDetails
import backend.model.event.EventRepository
import backend.model.event.TeamService
import backend.model.user.Participant
import backend.view.TeamView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.*
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST

@RestController
@RequestMapping("/event/{id}/team")
class TeamController {

    lateinit var teamService: TeamService
    lateinit var eventRepository: EventRepository

    @Autowired
    constructor(teamService: TeamService, eventRepository: EventRepository) {
        this.teamService = teamService
        this.eventRepository = eventRepository
    }

    @ResponseStatus(CREATED)
    @RequestMapping("/", method = arrayOf(POST))
    fun createTeam(@PathVariable id: Long,
                   @AuthenticationPrincipal user: CustomUserDetails,
                   @RequestBody body: TeamView): TeamView {

        val event = eventRepository.findById(id) ?: throw ResourceNotFoundException("No event with id $id")
        val creator = user.getRole(Participant::class.java) as? Participant ?:
                throw UnauthorizedException("User is no participant")

        return TeamView(teamService.create(creator, body.name!!, body.description!!, event))
    }
}

@ResponseStatus(NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(UNAUTHORIZED)
class UnauthorizedException(message: String) : RuntimeException(message)
