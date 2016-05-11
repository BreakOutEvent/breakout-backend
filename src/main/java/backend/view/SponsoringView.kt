package backend.view

import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.sponsoring.UnregisteredSponsor
import javax.validation.Valid
import javax.validation.constraints.NotNull

class SponsoringView() {

    var teamId: Long? = null
    var team: String? = null

    @NotNull
    var amountPerKm: Double? = null

    @NotNull
    var limit: Double? = null

    var sponsorId: Long? = null

    var status: String? = null

    @Valid
    var unregisteredSponsor: UnregisteredSponsorView? = null

    constructor(sponsoring: Sponsoring) : this() {
        this.teamId = sponsoring.team?.id
        this.team = sponsoring.team?.name
        this.amountPerKm = sponsoring.amountPerKm.numberStripped.toDouble()
        this.limit = sponsoring.limit.numberStripped.toDouble()
        this.sponsorId = sponsoring.sponsor?.id

        this.status = when (sponsoring.status) {
            ACCEPTED -> "accepted"
            REJECTED -> "rejected"
            PROPOSED -> "proposed"
        }

        sponsoring.unregisteredSponsor?.let { this.unregisteredSponsor = UnregisteredSponsorView(it) }
    }
}

class UnregisteredSponsorView {

    @Valid
    @NotNull
    var address: UserView.AddressView? = null

    @NotNull
    var company: String? = null

    @NotNull
    var url: String? = null

    @NotNull
    var gender: String? = null

    @NotNull
    var firstname: String? = null

    @NotNull
    var lastname: String? = null

    @NotNull
    var isHidden = false

    constructor(unregisteredSponsor: UnregisteredSponsor) {
        this.address = UserView.AddressView(unregisteredSponsor.address)
        this.company = unregisteredSponsor.company
        this.url = unregisteredSponsor.url.toString()
        this.gender = unregisteredSponsor.firstname
        this.firstname = unregisteredSponsor.firstname
        this.lastname = unregisteredSponsor.lastname
        this.isHidden = unregisteredSponsor.isHidden
    }
}

