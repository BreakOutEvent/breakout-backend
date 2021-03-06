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
import backend.services.ConfigurationService
import backend.view.SponsorTeamProfileView
import backend.view.UnregisteredSponsorView
import backend.view.sponsoring.SponsoringTeamProfileView
import backend.view.sponsoring.SponsoringView
import org.javamoney.moneta.Money
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class SponsoringController(private var sponsoringService: SponsoringService,
                           private var userService: UserService,
                           private var teamService: TeamService,
                           private var configurationService: ConfigurationService) {

    private val jwtSecret: String = configurationService.getRequired("org.breakout.api.jwt_secret")

    /**
     * GET /event/{eventId}/team/{teamId}/sponsoring/
     * Get a list of all sponsorings for the team with teamId
     */
    @GetMapping("/event/{eventId}/team/{teamId}/sponsoring/")
    fun getAllSponsorings(@AuthenticationPrincipal customUserDetails: CustomUserDetails?,
                          @PathVariable teamId: Long): Iterable<SponsoringView> {

        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId found")
        return if (customUserDetails != null) getAllSponsoringsAuthenticated(customUserDetails, team)
        else getAllSponsoringsUnauthenticated(team)
    }

    private fun getAllSponsoringsAuthenticated(customUserDetails: CustomUserDetails, team: Team): Iterable<SponsoringView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class)

        if (participant != null && team.isMember(participant)) {
            return sponsoringService.findByTeamId(team.id!!).map(::SponsoringView)
        } else {
            throw UnauthorizedException("Only members of the team ${team.id} can view its sponsorings")
        }
    }

    private fun getAllSponsoringsUnauthenticated(team: Team): Iterable<SponsoringView> {
        return sponsoringService.findByTeamId(team.id!!).map { sponsoring ->
            val view = SponsoringView(sponsoring)

            sponsoring.sponsor?.unregisteredSponsor?.let {
                if (it.isHidden) {
                    view.unregisteredSponsor = null
                    view.sponsorIsHidden = true
                }
                view.unregisteredSponsor?.address = null
                view.unregisteredSponsor?.email = null
            }

            sponsoring.sponsor?.let {
                if (it.isHidden) {
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
    @PostMapping("/event/{eventId}/team/{teamId}/sponsoring/")
    @ResponseStatus(CREATED)
    fun createSponsoring(@PathVariable teamId: Long,
                         @Valid @RequestBody body: SponsoringView,
                         @AuthenticationPrincipal customUserDetails: CustomUserDetails): SponsoringView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val team = teamService.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")
        val amountPerKm = Money.of(body.amountPerKm, "EUR")
        val limit = Money.of(body.limit, "EUR")

        val sponsoring = if (body.unregisteredSponsor != null) {
            user.getRole(Participant::class) ?: throw UnauthorizedException("Cannot add unregistered sponsor if user is no participant")
            createSponsoringWithUnregisteredSponsor(team, amountPerKm, limit, body.unregisteredSponsor!!)
        } else {
            val sponsor = user.getRole(Sponsor::class) ?: throw UnauthorizedException("Cannot add user as sponsor. Missing role sponsor")
            createSponsoringWithAuthenticatedSponsor(team, amountPerKm, limit, sponsor)
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
                address = sponsor.address!!.toAddress()!!,
                email = sponsor.email,
                isHidden = sponsor.isHidden)

        return sponsoringService.createSponsoringWithOfflineSponsor(team, amount, limit, unregisteredSponsor)
    }


    /**
     * GET /user/{userId}/sponsor/sponsoring/
     * Get a list of all sponsorings for the user with userId
     * This can only be done if the user is a sponsor
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}/sponsor/sponsoring/")
    fun getAllSponsoringsForSponsor(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                                    @PathVariable userId: Long): Iterable<SponsoringView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.account.id != userId) throw UnauthorizedException("A sponsor can only see it's own sponsorings")

        val sponsorings = sponsoringService.findBySponsorId(userId)
        return sponsorings.map(::SponsoringView)
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/event/{eventId}/team/{teamId}/sponsoring/{sponsoringId}/status/")
    fun acceptOrRejectSponsoring(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                                 @PathVariable sponsoringId: Long,
                                 @RequestBody body: Map<String, String>): SponsoringView {

        val sponsoring = sponsoringService.findOne(sponsoringId) ?: throw NotFoundException("No sponsoring with id $sponsoringId found")
        val status = body["status"] ?: throw BadRequestException("Missing status in body")

        return when (status.toLowerCase()) {
            "accepted" -> SponsoringView(sponsoringService.acceptSponsoring(sponsoring))
            "rejected" -> SponsoringView(sponsoringService.rejectSponsoring(sponsoring))
            "withdrawn" -> SponsoringView(sponsoringService.withdrawSponsoring(sponsoring))
            else -> throw BadRequestException("Invalid status $status")
        }
    }

    @GetMapping("/team/{teamId}/sponsoring/")
    fun getAllSponsoringsForTeamOverview(@PathVariable teamId: Long): Iterable<SponsoringTeamProfileView> {
        return sponsoringService.findByTeamId(teamId).map {
            val sponsor = when (it.sponsor?.isHidden ?: true) {
                true -> SponsorTeamProfileView(
                        sponsorId = null,
                        firstname = "",
                        lastname = "",
                        company = null,
                        sponsorIsHidden = it.sponsor?.isHidden ?: true,
                        url = null,
                        logoUrl = null)
                false -> SponsorTeamProfileView(
                        sponsorId = it.sponsor?.registeredSponsor?.id,
                        firstname = it.sponsor?.firstname ?: "",
                        lastname = it.sponsor?.lastname ?: "",
                        company = it.sponsor?.company,
                        sponsorIsHidden = it.sponsor?.isHidden ?: true,
                        url = it.sponsor?.url,
                        logoUrl = it.sponsor?.logo?.url)
            }

            SponsoringTeamProfileView(sponsor, it.status.toString())
        }
    }

}

