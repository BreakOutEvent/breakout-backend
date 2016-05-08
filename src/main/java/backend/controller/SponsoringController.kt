package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.TeamService
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringService
import backend.model.user.Sponsor
import backend.model.user.UserService
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/event/{eventId}/team/{teamId}/sponsoring")
open class SponsoringController {

    private var sponsoringService: SponsoringService
    private var userService: UserService
    private var teamService: TeamService

    @Autowired
    constructor(sponsoringService: SponsoringService,
                userService: UserService,
                teamService: TeamService) {

        this.sponsoringService = sponsoringService
        this.userService = userService
        this.teamService = teamService
    }

    // TODO: Who is authorized to do this?
    @RequestMapping("/", method = arrayOf(GET))
    open fun getAllSponsorings(@PathVariable teamId: Long): Iterable<SponsoringView> {
        return sponsoringService.findByTeamId(teamId).map { SponsoringView(it) }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createSponsoring(@PathVariable teamId: Long,
                              @Valid @RequestBody sponsoringView: SponsoringView,
                              @AuthenticationPrincipal customUserDetails: CustomUserDetails): SponsoringView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val sponsor = user.getRole(Sponsor::class) ?: throw UnauthorizedException("User is no sponsor")
        val team = teamService.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val amountPerKm = Money.of(sponsoringView.amountPerKm, "EUR")
        val limit = Money.of(sponsoringView.limit, "EUR")

        val sponsoring = sponsoringService.createSponsoring(sponsor, team, amountPerKm, limit)
        return SponsoringView(sponsoring)
    }
}

class SponsoringView() {

    var teamId: Long? = null
    var team: String? = null

    @NotNull
    var amountPerKm: Double? = null

    @NotNull
    var limit: Double? = null

    var sponsorId: Long? = null

    constructor(sponsoring: Sponsoring) : this() {
        this.teamId = sponsoring.team?.id
        this.team = sponsoring.team?.name
        this.amountPerKm = sponsoring.amountPerKm.numberStripped.toDouble()
        this.limit = sponsoring.limit.numberStripped.toDouble()
        this.sponsorId = sponsoring.sponsor?.id
    }
}
