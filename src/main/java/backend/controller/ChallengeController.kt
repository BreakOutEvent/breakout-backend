package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.challenges.ChallengeService
import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.posting.PostingService
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.euroOf
import backend.view.ChallengeStatusView
import backend.view.ChallengeView
import org.javamoney.moneta.Money
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class ChallengeController(private var challengeService: ChallengeService,
                          private var userService: UserService,
                          private var teamService: TeamService,
                          private var postingService: PostingService,
                          private var configurationService: ConfigurationService) {

    private var jwtSecret: String = configurationService.getRequired("org.breakout.api.jwt_secret")


    /**
     * GET /user/{userId}/sponsor/challenge/
     * Get a list of all sponsorings for the user with userId
     * This can only be done if the user is a sponsor
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}/sponsor/challenge/")
    fun getAllChallengesForSponsor(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                                   @PathVariable userId: Long): Iterable<ChallengeView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)

        if (user.account.id != userId) throw UnauthorizedException("A sponsor can only see it's own challenges")

        val challenges = challengeService.findBySponsorId(userId)
        return challenges.map(::ChallengeView)
    }

    /**
     * POST /event/{eventId}/team/{teamId}/challenge/
     * Propose a challenge to a team. This can only be done
     * when being a sponsor or when providing data for an unregistered sponsor
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/event/{eventId}/team/{teamId}/challenge/")
    @ResponseStatus(CREATED)
    fun createChallenge(@AuthenticationPrincipal customUserDetails: CustomUserDetails,
                        @PathVariable teamId: Long,
                        @Valid @RequestBody body: ChallengeView): ChallengeView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId found")
        val amount = euroOf(body.amount!!)
        val description = body.description!!

        val challenge = if (body.unregisteredSponsor != null) {
            challengeUnregisteredSponsor(body, team, amount, description)
        } else {
            challengeWithRegisteredSponsor(user, team, amount, description)
        }

        return challenge
    }

    private fun challengeWithRegisteredSponsor(user: User, team: Team, amount: Money, description: String): ChallengeView {
        val sponsor = user.getRole(Sponsor::class) ?: throw UnauthorizedException("User is no sponsor")
        val challenge = challengeService.proposeChallenge(sponsor, team, amount, description)
        challenge.contract.generateSignedUploadToken(jwtSecret)
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
        challenge.contract.generateSignedUploadToken(jwtSecret)
        return ChallengeView(challenge)
    }

    /**
     * PUT /event/{eventId}/team/{teamId}/challenge/{challengeId}/status/
     * Accept, reject or add proof to a challenge
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/event/{eventId}/team/{teamId}/challenge/{challengeId}/status/")
    fun changeStatus(@PathVariable challengeId: Long,
                     @Valid @RequestBody body: ChallengeStatusView): ChallengeView {

        val challenge = challengeService.findOne(challengeId) ?: throw NotFoundException("No challenge with id $challengeId found")
        return when (body.status!!.toLowerCase()) {
            "accepted" -> challengeService.accept(challenge)
            "rejected" -> challengeService.reject(challenge)
            "withdrawn" -> challengeService.withdraw(challenge)
            "with_proof" -> {
                val proof = postingService.getByID(body.postingId!!) ?: throw NotFoundException("No posting with id ${body.postingId} found")
                challengeService.addProof(challenge, proof)
            }
            else -> throw BadRequestException("Unknown status for challenge ${body.status}")
        }.let(::ChallengeView)
    }

    /**
     * GET /event/{eventId}/team/{teamId}/challenge/
     * Get all challenges for a team
     */
    @GetMapping("/event/{eventId}/team/{teamId}/challenge/")
    fun getAllChallengesForTeam(@PathVariable teamId: Long): Iterable<ChallengeView> {
        return challengeService.findByTeamId(teamId).map {
            val view = ChallengeView(it)
            view.unregisteredSponsor?.address = null // Set address to null to make it non public
            return@map view
        }
    }
}

