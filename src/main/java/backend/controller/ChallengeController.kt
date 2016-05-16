package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeService
import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.posting.PostingService
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Participant
import backend.model.user.Sponsor
import backend.model.user.User
import backend.model.user.UserService
import backend.util.euroOf
import backend.view.UnregisteredSponsorView
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@RestController
open class ChallengeController {

    private var challengeService: ChallengeService
    private var postingService: PostingService
    private var teamService: TeamService
    private var userService: UserService

    @Autowired
    constructor(challengeService: ChallengeService, userService: UserService, teamService: TeamService, postingService: PostingService) {
        this.challengeService = challengeService
        this.userService = userService
        this.teamService = teamService
        this.postingService = postingService
    }

    /**
     * POST /event/{eventId}/team/{teamId}/challenge/
     * Propose a challenge to a team. This can only be done
     * when being a sponsor or when providing data for an unregistered sponsor
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/event/{eventId}/team/{teamId}/challenge/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createChallengeS(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                              @PathVariable teamId: Long,
                              @Valid @RequestBody body: ChallengeView): ChallengeView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId found")
        val amount = euroOf(body.amount!!)
        val description = body.description!!

        if (user.hasRole(Sponsor::class)) return challengeWithRegisteredSponsor(user, team, amount, description)
        else if (user.hasRole(Participant::class)) return challengeUnregisteredSponsor(body, team, amount, description)
        else throw UnauthorizedException("User in neither Participant nor sponsor")
    }

    private fun challengeWithRegisteredSponsor(user: User, team: Team, amount: Money, description: String): ChallengeView {
        val sponsor = user.getRole(Sponsor::class) ?: throw UnauthorizedException("User is no sponsor")
        val challenge = challengeService.proposeChallenge(sponsor, team, amount, description)
        return ChallengeView(challenge)
    }

    private fun challengeUnregisteredSponsor(body: ChallengeView, team: Team, amount: Money, description: String): ChallengeView {
        val unregisteredSponsor = body.unregisteredSponsor ?: throw BadRequestException("Missing data for unregistered sponsor")

        val sponsor = UnregisteredSponsor(
                firstname = unregisteredSponsor.firstname!!,
                lastname = unregisteredSponsor.lastname!!,
                company = unregisteredSponsor.company!!,
                gender = unregisteredSponsor.gender!!,
                url = unregisteredSponsor.url!!,
                address = unregisteredSponsor.address!!.toAddress()!!,
                isHidden = unregisteredSponsor.isHidden)

        val challenge = challengeService.proposeChallenge(sponsor, team, amount, description)
        return ChallengeView(challenge)
    }

    /**
     * PUT /event/{eventId}/team/{teamId}/challenge/{challengeId}/status/
     * Accept, reject or add proof to a challenge
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/event/{eventId}/team/{teamId}/challenge/{challengeId}/status/")
    open fun changeStatus(@PathVariable challengeId: Long,
                          @Valid @RequestBody body: ChallengeStatusView): ChallengeView {

        val challenge = challengeService.findOne(challengeId) ?: throw NotFoundException("No challenge with id $challengeId found")
        return when (body.status!!) {
            "accepted" -> challengeService.accept(challenge)
            "rejected" -> challengeService.reject(challenge)
            "with_proof" -> {
                val proof = postingService.getByID(body.postingId!!) ?: throw NotFoundException("No posting with id ${body.postingId} found")
                challengeService.addProof(challenge, proof)
            }

            else -> throw BadRequestException("Unknown status for challenge ${body.status}")
        }.let { ChallengeView(it) }
    }
}

class ChallengeStatusView {
    @NotNull var status: String? = null
    var postingId: Long? = null
}


class ChallengeView {

    var status: String? = null

    var teamId: Long? = null

    var team: String? = null

    var sponsorId: Long? = null

    @Valid
    var unregisteredSponsor: UnregisteredSponsorView? = null

    @NotNull
    var amount: Double? = null

    @NotNull
    @Size(max = 1000)
    var description: String? = null

    /**
     * no-args constructor for Jackson
     */
    constructor()

    constructor(challenge: Challenge) {
        this.description = challenge.description
        this.amount = challenge.amount.numberStripped.toDouble()
        this.sponsorId = challenge.sponsor?.id
        this.teamId = challenge.team!!.id!!
        this.team = challenge.team!!.name
        this.status = challenge.status.toString()
        this.unregisteredSponsor = challenge.unregisteredSponsor?.let { UnregisteredSponsorView(it) }
    }
}
