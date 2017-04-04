package backend.view

import backend.model.payment.TeamEntryFeeInvoice

class TeamEntryFeeInvoiceView {

    var id: Long? = null
    var amount: Double? = null
    var team: Long? = null
    var payments: List<PaymentView> = listOf()
    var purposeOfTransfer: String? = null

    constructor()

    constructor(invoice: TeamEntryFeeInvoice) {
        this.id = invoice.id
        this.amount = invoice.amount.numberStripped.toDouble()
        this.team = invoice.team?.id
        this.payments = invoice.getPayments().map(::PaymentView)
        this.purposeOfTransfer = invoice.purposeOfTransfer
    }
}
