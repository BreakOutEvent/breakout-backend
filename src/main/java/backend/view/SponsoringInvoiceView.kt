package backend.view

import backend.model.payment.SponsoringInvoice

class SponsoringInvoiceView {

    var id: Long? = null
    var amount: Double? = null
    var teamId: Long? = null
    var subject: String? = null
    var firstname: String? = null
    var lastname: String? = null
    var company: String? = null
    var payments: List<PaymentView> = listOf()
    var sponsorings: List<DetailedSponsoringView> = listOf()
    var challenges: List<DetailedChallengeView> = listOf()

    constructor()

    constructor(invoice: SponsoringInvoice) {
        this.id = invoice.id
        this.amount = invoice.amount.numberStripped.toDouble()
        this.teamId = invoice.team?.id
        this.subject = invoice.subject
        this.firstname = invoice.firstname
        this.lastname = invoice.lastname
        this.company = invoice.company
        this.payments = invoice.getPayments().map { PaymentView(it) }
        this.challenges = invoice.challenges.map { DetailedChallengeView(it) }
        this.sponsorings = invoice.sponsorings.map { DetailedSponsoringView(it) }
    }
}
