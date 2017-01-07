package backend.view

import backend.model.challenges.Challenge
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class DetailedChallengeView {

    var id: Long? = null

    var eventId: Long? = null

    var status: String? = null

    var teamId: Long? = null

    var team: String? = null

    var sponsorId: Long? = null

    var userId: Long? = null

    var firstname: String? = null

    var lastname: String? = null

    var company: String? = null

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
        // TODO: Make this view "smaller" and refactor by using ISponsor
        challenge.getSponsor().sponsorRole?.isHidden?.let {
            if (it) {
                this.sponsorIsHidden = true
                this.contract = null
            } else {
                this.userId = challenge.getSponsor().sponsorRole?.account?.id
                this.sponsorId = challenge.getSponsor().sponsorRole?.id
                this.firstname = challenge.getSponsor().sponsorRole?.firstname
                this.lastname = challenge.getSponsor().sponsorRole?.lastname
                this.company = challenge.getSponsor().sponsorRole?.company
            }
        }

        // Add information about unregistered sponsor
        // if he exists and isHidden is false
        challenge.getSponsor().unregisteredSponsor?.isHidden?.let {
            if (it) {
                this.sponsorIsHidden = true
                this.contract = null
            } else {
                this.unregisteredSponsor = UnregisteredSponsorView(challenge.getSponsor().unregisteredSponsor!!)
            }
        }
    }
}
