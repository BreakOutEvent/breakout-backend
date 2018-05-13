package backend.view

import backend.model.sponsoring.UnregisteredSponsor
import backend.view.user.UserView
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import javax.validation.Valid
import javax.validation.constraints.NotNull

class UnregisteredSponsorView() {

    @Valid
    @NotNull
    var address: UserView.AddressView? = null

    @NotNull
    @SafeHtml(whitelistType = NONE)
    var company: String? = null

    @NotNull
    @SafeHtml(whitelistType = NONE)
    var url: String? = null


    @NotNull
    @SafeHtml(whitelistType = NONE)
    var firstname: String? = null

    @NotNull
    @SafeHtml(whitelistType = NONE)
    var lastname: String? = null

    @NotNull
    var isHidden = false

    @Email
    var email: String? = null

    constructor(unregisteredSponsor: UnregisteredSponsor) : this() {
        this.address = UserView.AddressView(unregisteredSponsor.address)
        this.company = unregisteredSponsor.company
        this.url = unregisteredSponsor.url.toString()
        this.firstname = unregisteredSponsor.firstname
        this.lastname = unregisteredSponsor.lastname
        this.isHidden = unregisteredSponsor.isHidden
        this.email = unregisteredSponsor.email
    }
}
