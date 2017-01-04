package backend.view

import backend.model.challenges.Challenge
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class ChallengeView {

    var id: Long? = null

    var eventId: Long? = null

    var status: String? = null

    var teamId: Long? = null

    var team: String? = null

    var sponsorId: Long? = null

    var userId: Long? = null

    var sponsorIsHidden: Boolean = false

    @Valid
    var unregisteredSponsor: UnregisteredSponsorView? = null

    @NotNull
    var amount: Double? = null

    @NotNull
    @Size(max = 1000)
    var description: String? = null

    var contract: MediaView? = null

    /**
     * no-args constructor for Jackson
     */
    constructor()

    constructor(challenge: Challenge) {
        this.id = challenge.id!!
        this.eventId = challenge.team!!.event.id!!
        this.description = challenge.description
        this.amount = challenge.amount.numberStripped.toDouble()
        this.teamId = challenge.team!!.id!!
        this.team = challenge.team!!.name
        this.status = challenge.status.toString().toUpperCase()
        this.contract = MediaView(challenge.contract)

        // Add information about registered sponsor
        // if he exists and isHidden is false
        challenge.sponsor?.isHidden?.let {
            if (it) {
                this.sponsorIsHidden = true
                this.contract = null
            } else {
                this.userId = challenge.sponsor?.account?.id
                this.sponsorId = challenge.sponsor?.id
            }
        }

        // Add information about unregistered sponsor
        // if he exists and isHidden is false
        challenge.unregisteredSponsor?.isHidden?.let {
            if (it) {
                this.sponsorIsHidden = true
                this.contract = null
            } else {
                this.unregisteredSponsor = UnregisteredSponsorView(challenge.unregisteredSponsor!!)
            }
        }
    }
}
