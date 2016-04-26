package backend.view

import backend.model.payment.TeamEntryFeeInvoice

class TeamEntryFeeInvoiceView {

    var amount: Double? = null
    var team: Long? = null
    var payments: List<PaymentView> = listOf()

    constructor()

    constructor(invoice: TeamEntryFeeInvoice) {
        this.amount = invoice.amount.numberStripped.toDouble()
        this.team = invoice.team?.id
        this.payments = invoice.getPayments().map { PaymentView(it) }
    }
}
