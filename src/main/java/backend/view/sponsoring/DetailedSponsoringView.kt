package backend.view.sponsoring

import backend.model.sponsoring.Sponsoring
import backend.view.MediaView
import backend.view.UnregisteredSponsorView
import javax.validation.Valid
import javax.validation.constraints.NotNull

class DetailedSponsoringView() {

    var id: Long? = null

    var eventId: Long? = null

    var teamId: Long? = null

    var team: String? = null

    @NotNull
    var amountPerKm: Double? = null

    @NotNull
    var limit: Double? = null

    var sponsorId: Long? = null

    var userId: Long? = null

    var firstname: String? = null

    var lastname: String? = null

    var company: String? = null

    var status: String? = null

    var contract: MediaView? = null

    @Valid
    var unregisteredSponsor: UnregisteredSponsorView? = null

    var sponsorIsHidden: Boolean = false

    constructor(sponsoring: Sponsoring) : this() {
        this.id = sponsoring.id
        this.eventId = sponsoring.team?.event?.id
        this.teamId = sponsoring.team?.id
        this.team = sponsoring.team?.name
        this.amountPerKm = sponsoring.amountPerKm.numberStripped.toDouble()
        this.limit = sponsoring.limit.numberStripped.toDouble()
        this.status = sponsoring.status.toString().toUpperCase()

        this.contract = sponsoring.contract?.let(::MediaView)

        // Add information about registered sponsor
        // if he exists and isHidden is false
        sponsoring.sponsor?.registeredSponsor?.isHidden?.let {
            if (it) {
                this.sponsorIsHidden = true
                this.contract = null
            } else {
                this.userId = sponsoring.sponsor?.registeredSponsor?.account?.id
                this.sponsorId = sponsoring.sponsor?.registeredSponsor?.id

                this.firstname = sponsoring.sponsor?.firstname
                this.lastname = sponsoring.sponsor?.lastname
                this.company = sponsoring.sponsor?.company
            }
        }

        // Add information about unregistered sponsor
        // if he exists and isHidden is false
        sponsoring.sponsor?.unregisteredSponsor?.isHidden?.let {
            if (it) {
                this.sponsorIsHidden = true
                this.contract = null
            } else {
                this.unregisteredSponsor = UnregisteredSponsorView(sponsoring.sponsor?.unregisteredSponsor!!)
            }
        }
    }
}

