package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.challenges.ChallengeService
import backend.model.event.Team
import backend.model.event.TeamService
import backend.model.posting.PostingService
import backend.model.removeBlockedBy
import backend.model.removeReported
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.CacheNames.POSTINGS
import backend.util.euroOf
import backend.view.SponsorTeamProfileView
import backend.view.challenge.ChallengeStatusView
import backend.view.challenge.ChallengeTeamProfileView
import backend.view.challenge.ChallengeView
import backend.view.posting.PostingResponseView
import org.javamoney.moneta.Money
import org.springframework.cache.annotation.CacheEvict
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
    @CacheEvict(value = POSTINGS, allEntries = true)
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

        return if (body.unregisteredSponsor != null) {
            challengeUnregisteredSponsor(body, team, amount, description)
        } else {
            challengeWithRegisteredSponsor(user, team, amount, description, body.maximumCount)
        }
    }

    private fun challengeWithRegisteredSponsor(user: User, team: Team, amount: Money, description: String, maximumCount: Int?): ChallengeView {
        val sponsor = user.getRole(Sponsor::class) ?: user.addRole(Sponsor::class)
        val challenge = challengeService.proposeChallenge(sponsor, team, amount, description, maximumCount)
        return ChallengeView(challenge)
    }

    private fun challengeUnregisteredSponsor(body: ChallengeView, team: Team, amount: Money, description: String): ChallengeView {
        val unregisteredSponsor = body.unregisteredSponsor
                ?: throw BadRequestException("Missing data for unregistered sponsor")

        val sponsor = UnregisteredSponsor(
                firstname = unregisteredSponsor.firstname!!,
                lastname = unregisteredSponsor.lastname!!,
                company = unregisteredSponsor.company!!,
                email = unregisteredSponsor.email,
                address = unregisteredSponsor.address!!.toAddress()!!,
                isHidden = unregisteredSponsor.isHidden)

        val challenge = challengeService.proposeChallenge(sponsor, team, amount, description, body.maximumCount)
        return ChallengeView(challenge)
    }

    /**
     * PUT /event/{eventId}/team/{teamId}/challenge/{challengeId}/status/
     * Accept, reject or add proof to a challenge
     */
    @CacheEvict(value = POSTINGS, allEntries = true)
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/event/{eventId}/team/{teamId}/challenge/{challengeId}/status/")
    fun changeStatus(@PathVariable challengeId: Long,
                     @Valid @RequestBody body: ChallengeStatusView): ChallengeView {

        val challenge = challengeService.findOne(challengeId)
                ?: throw NotFoundException("No challenge with id $challengeId found")
        return when (body.status!!.toLowerCase()) {
            "rejected" -> challengeService.reject(challenge)
            "withdrawn" -> challengeService.withdraw(challenge)
            "with_proof" -> {
                val proof = postingService.getByID(body.postingId!!)
                        ?: throw NotFoundException("No posting with id ${body.postingId} found")
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
    fun getAllChallengesForTeam(@AuthenticationPrincipal customUserDetails: CustomUserDetails?,
                                @PathVariable teamId: Long): Iterable<ChallengeView> {
        val team = teamService.findOne(teamId) ?: throw NotFoundException("No team with id $teamId found")

        return if (customUserDetails != null) getAllChallengesAuthenticated(customUserDetails, team)
        else getAllChallengesUnauthenticated(team)
    }


    private fun getAllChallengesAuthenticated(customUserDetails: CustomUserDetails, team: Team): Iterable<ChallengeView> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val participant = user.getRole(Participant::class)

        if (participant != null && team.isMember(participant)) {
            return challengeService.findByTeamId(team.id!!).map(::ChallengeView)
        } else {
            throw UnauthorizedException("Only members of the team ${team.id} can view its challenges")
        }
    }

    private fun getAllChallengesUnauthenticated(team: Team): Iterable<ChallengeView> {
        return challengeService.findByTeamId(team.id!!).map { challenge ->
            val view = ChallengeView(challenge)

            challenge.sponsor.unregisteredSponsor?.let {
                if (it.isHidden) {
                    view.unregisteredSponsor = null
                    view.sponsorIsHidden = true
                }
                view.unregisteredSponsor?.address = null
                view.unregisteredSponsor?.email = null
            }

            challenge.sponsor.let {
                if (it.isHidden) {
                    view.sponsorId = null
                    view.sponsorIsHidden = true
                }
            }

            return@map view
        }
    }

    @GetMapping("/team/{teamId}/challenge/")
    fun getAllChallengesForTeamProfile(@PathVariable teamId: Long): Iterable<ChallengeTeamProfileView> {
        return challengeService.findByTeamId(teamId).map {

            val sponsor = when (it.sponsor.isHidden) {
                true -> SponsorTeamProfileView(
                        sponsorId = null,
                        firstname = "",
                        lastname = "",
                        company = null,
                        sponsorIsHidden = it.sponsor.isHidden,
                        url = null,
                        logoUrl = null)
                false -> SponsorTeamProfileView(
                        sponsorId = it.sponsor.registeredSponsor?.id,
                        firstname = it.sponsor.firstname ?: "",
                        lastname = it.sponsor.lastname ?: "",
                        company = it.sponsor.company,
                        sponsorIsHidden = it.sponsor.isHidden,
                        url = it.sponsor.url,
                        logoUrl = it.sponsor.logo?.url)
            }

            ChallengeTeamProfileView(it.id, it.amount, it.description, it.status.toString(), it.fulfilledCount, it.maximumCount, sponsor)
        }
    }

    @GetMapping("/challenge/{challengeId}/posting/")
    fun getPostingsFulfullingChallenges(@PathVariable challengeId: Long, @AuthenticationPrincipal customUserDetails: CustomUserDetails?): Iterable<PostingResponseView> {
        return postingService
                .findAllByChallenge(challengeId)
                .removeReported()
                .removeBlockedBy(customUserDetails?.id)
                .map {
                    PostingResponseView(it.hasLikesBy(customUserDetails?.id), it.challenge?.let {
                        challengeService.findChallengeProveProjectionById(it)
                    }, customUserDetails?.id)
                }
    }

}

