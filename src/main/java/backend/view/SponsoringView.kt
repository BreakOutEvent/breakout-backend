package backend.view

import backend.model.sponsoring.Sponsoring
import javax.validation.constraints.NotNull

class SponsoringView() {

    var teamId: Long? = null
    var team: String? = null

    @NotNull
    var amountPerKm: Double? = null

    @NotNull
    var limit: Double? = null

    var sponsorId: Long? = null

    constructor(sponsoring: Sponsoring) : this() {
        this.teamId = sponsoring.team?.id
        this.team = sponsoring.team?.name
        this.amountPerKm = sponsoring.amountPerKm.numberStripped.toDouble()
        this.limit = sponsoring.limit.numberStripped.toDouble()
        this.sponsorId = sponsoring.sponsor?.id
    }
}

