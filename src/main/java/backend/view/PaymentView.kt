package backend.view

import backend.model.payment.Payment
import java.time.ZoneOffset
import javax.validation.constraints.NotNull

class PaymentView {

    @NotNull
    var amount: Double? = null

    var paymentMethod: String? = null

    var user: Long? = null

    var fidorId: Long? = null

    var date: Long? = null

    constructor()

    constructor(payment: Payment) {
        this.amount = payment.amount.numberStripped.toDouble()
        this.paymentMethod = payment.getPaymentMethod()
        this.user = payment.user()!!.account.id
        this.fidorId = payment.fidorId
        this.date = payment.date?.toEpochSecond(ZoneOffset.UTC)
    }
}
