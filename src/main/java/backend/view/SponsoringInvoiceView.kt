package backend.view

import backend.model.payment.SponsoringInvoice

class SponsoringInvoiceView {

    var amount: Double? = null
    var team: Long? = null
    var subject: String? = null
    var payments: List<PaymentView> = listOf()
    var sponsorings: List<SponsoringView> = listOf()
    var challenges: List<ChallengeView> = listOf()

    constructor()

    constructor(invoice: SponsoringInvoice) {
        this.amount = invoice.amount.numberStripped.toDouble()
        this.team = invoice.team?.id
        this.subject = invoice.subject
        this.payments = invoice.getPayments().map { PaymentView(it) }
        this.challenges = invoice.challenges.map { ChallengeView(it) }
        this.sponsorings = invoice.sponsorings.map { SponsoringView(it) }
    }
}
