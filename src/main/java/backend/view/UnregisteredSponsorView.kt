package backend.view

import backend.model.sponsoring.UnregisteredSponsor
import javax.validation.Valid
import javax.validation.constraints.NotNull

class UnregisteredSponsorView() {

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

    constructor(unregisteredSponsor: UnregisteredSponsor) : this() {
        this.address = UserView.AddressView(unregisteredSponsor.address)
        this.company = unregisteredSponsor.company
        this.url = unregisteredSponsor.url.toString()
        this.gender = unregisteredSponsor.gender
        this.firstname = unregisteredSponsor.firstname
        this.lastname = unregisteredSponsor.lastname
        this.isHidden = unregisteredSponsor.isHidden
    }
}
