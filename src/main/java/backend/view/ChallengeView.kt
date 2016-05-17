package backend.view

import backend.model.challenges.Challenge
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class ChallengeView {

    var status: String? = null

    var teamId: Long? = null

    var team: String? = null

    var sponsorId: Long? = null

    var sponsorIsHidden: Boolean = false

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
        this.teamId = challenge.team!!.id!!
        this.team = challenge.team!!.name
        this.status = challenge.status.toString()

        // Add information about registered sponsor
        // if he exists and isHidden is false
        challenge.sponsor?.isHidden?.let {
            if (it) this.sponsorIsHidden = true
            else this.sponsorId = challenge.sponsor?.id
        }

        // Add information about unregistered sponsor
        // if he exists and isHidden is false
        challenge.unregisteredSponsor?.isHidden?.let {
            if (it) this.sponsorIsHidden = true
            else this.unregisteredSponsor = UnregisteredSponsorView(challenge.unregisteredSponsor!!)
        }
    }
}
