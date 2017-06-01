package backend.view.sponsoring

import backend.model.payment.SponsoringInvoice
import backend.model.sponsoring.ISponsor
import backend.view.user.UserView

class SponsoringInvoiceView {

    var id: Long? = null
    var amount: Double? = null
    var payed: Double? = null
    var sponsor: SponsorView? = null
    var purposeOfTransfer: String? = null

    constructor()

    constructor(invoice: SponsoringInvoice) {
        this.id = invoice.id
        this.amount = invoice.amount.numberStripped.toDouble()
        this.payed = invoice.amountOfCurrentPayments().numberStripped.toDouble()
        this.sponsor = SponsorView(invoice.sponsor)
        this.purposeOfTransfer = invoice.purposeOfTransfer
    }
}

class SponsorView {

    var firstname: String? = null
    var lastname: String? = null
    var address: UserView.AddressView? = null
    var company: String? = null
    var email: String? = null
    var url: String? = null

    constructor()

    constructor(sponsor: ISponsor) {
        this.firstname = sponsor.firstname
        this.lastname = sponsor.lastname
        this.address = UserView.AddressView(sponsor.address)
        this.company = sponsor.company
        this.email = sponsor.registeredSponsor?.email ?: sponsor.unregisteredSponsor?.email
        this.url = sponsor.url.toString()
    }
}
