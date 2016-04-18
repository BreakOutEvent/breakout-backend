package backend.model.payment

import backend.model.user.User
import org.javamoney.moneta.Money

abstract class Payment {

    var amount: Money
        private set

    var user: User
        private set

    constructor(amount: Money, user: User) {
        this.amount = amount
        this.user = user
    }

    abstract fun getPaymentMethod(): String
}
