package backend.view

import backend.model.payment.Payment
import javax.validation.constraints.NotNull

class PaymentView {

    @NotNull
    var amount: Double? = null

    var paymentMethod: String? = null

    var user: Long? = null

    constructor()

    constructor(payment: Payment) {
        this.amount = payment.amount.numberStripped.toDouble()
        this.paymentMethod = payment.getPaymentMethod()
        this.user = payment.user().core.id
    }
}
