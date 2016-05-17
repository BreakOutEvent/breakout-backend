package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringService
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.model.user.UserService
import backend.view.SponsoringView
import backend.view.UnregisteredSponsorView
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import javax.validation.Valid

@RestController
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

    /**
     * GET /event/{eventId}/team/{teamId}/sponsoring/
     * Get a list of all sponsorings for the team with teamId
     */
    @RequestMapping("/event/{eventId}/team/{teamId}/sponsoring/", method = arrayOf(GET))
    open fun getAllSponsorings(@AuthenticationPrincipal customUserDetails: CustomUserDetails?,
                               @PathVariable teamId: Long): Iterable<SponsoringView> {

        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId found")
        if(customUserDetails != null) return getAllSponsoringsAuthenticated(customUserDetails, team)
        else return getAllSponsoringsUnauthenticated(team)
    }

    private fun getAllSponsoringsAuthenticated(customUserDetails: CustomUserDetails, team: Team): Iterable<SponsoringView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class)

        if (participant != null && team.isMember(participant)) {
            return sponsoringService.findByTeamId(team.id!!).map { SponsoringView(it) }
        } else {
            throw UnauthorizedException("Only members of the team ${team.id} can view its sponsorings")
        }
    }

    private fun getAllSponsoringsUnauthenticated(team: Team) : Iterable<SponsoringView> {
        return sponsoringService.findByTeamId(team.id!!).map { sponsoring ->
            val view = SponsoringView(sponsoring)

            sponsoring.unregisteredSponsor?.let {
                if(it.isHidden) {
                    view.unregisteredSponsor = null
                    view.sponsorIsHidden = true
                }
                view.unregisteredSponsor?.address = null
            }

            sponsoring.sponsor?.let {
                if(it.isHidden) {
                    view.sponsorId = null
                    view.sponsorIsHidden = true
                }
            }

            return@map view
        }
    }

    /**
     * POST /event/{eventId}/team/{teamId}/sponsoring/
     * Create a new sponsoring for the team with teamId
     * This can only done if user is a sponsor
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/event/{eventId}/team/{teamId}/sponsoring/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createSponsoring(@PathVariable teamId: Long,
                              @Valid @RequestBody sponsoringView: SponsoringView,
                              @AuthenticationPrincipal customUserDetails: CustomUserDetails): SponsoringView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val team = teamService.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val amountPerKm = Money.of(sponsoringView.amountPerKm, "EUR")
        val limit = Money.of(sponsoringView.limit, "EUR")

        val sponsoring = if (user.hasRole(Sponsor::class)) {
            val sponsor = user.getRole(Sponsor::class) ?: throw Exception("Can't get role sponsor")
            createSponsoringWithAuthenticatedSponsor(team, amountPerKm, limit, sponsor)
        } else if (user.hasRole(Participant::class)) {
            val unregisteredSponsorView = sponsoringView.unregisteredSponsor ?:
                    throw BadRequestException("User is no sponsor and no data for unregistered sponsor is provided")
            createSponsoringWithUnregisteredSponsor(team, amountPerKm, limit, unregisteredSponsorView)
        } else {
            throw BadRequestException("User is neither participant nor sponsor")
        }

        return SponsoringView(sponsoring)
    }

    private fun createSponsoringWithAuthenticatedSponsor(team: Team, amount: Money, limit: Money, sponsor: Sponsor): Sponsoring {
        return sponsoringService.createSponsoring(sponsor, team, amount, limit)
    }

    private fun createSponsoringWithUnregisteredSponsor(team: Team, amount: Money, limit: Money, sponsor: UnregisteredSponsorView): Sponsoring {

        val unregisteredSponsor = UnregisteredSponsor(
                firstname = sponsor.firstname!!,
                lastname = sponsor.lastname!!,
                company = sponsor.company!!,
                gender = sponsor.gender!!,
                url = sponsor.url!!,
                address = sponsor.address!!.toAddress()!!,
                isHidden = sponsor.isHidden)

        val sponsoring = sponsoringService.createSponsoringWithOfflineSponsor(team, amount, limit, unregisteredSponsor)
        return sponsoring
    }


    /**
     * GET /user/{userId}/sponsor/sponsoring/
     * Get a list of all sponsorings for the user with userId
     * This can only be done if the user is a sponsor
     */
    @RequestMapping("/user/{userId}/sponsor/sponsoring/", method = arrayOf(GET))
    open fun getAllSponsoringsForSponsor(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                                         @PathVariable userId: Long): Iterable<SponsoringView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.core.id != userId) throw UnauthorizedException("A sponsor can only see it's own sponsorings")

        val sponsorings = sponsoringService.findBySponsorId(userId)
        return sponsorings.map { SponsoringView(it) }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/event/{eventId}/team/{teamId}/sponsoring/{sponsoringId}/status/", method = arrayOf(PUT))
    open fun acceptOrRejectSponsoring(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                                      @PathVariable sponsoringId: Long,
                                      @RequestBody body: Map<String, String>): SponsoringView {

        val sponsoring = sponsoringService.findOne(sponsoringId) ?: throw NotFoundException("No sponsoring with id $sponsoringId found")
        val status = body["status"] ?: throw BadRequestException("Missing status in body")

        when (status) {
            "accepted" -> return SponsoringView(sponsoringService.acceptSponsoring(sponsoring))
            "rejected" -> return SponsoringView(sponsoringService.rejectSponsoring(sponsoring))
            else -> throw BadRequestException("Invalid status $status")
        }
    }
}

