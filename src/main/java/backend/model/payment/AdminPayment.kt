package backend.model.payment

import backend.model.user.Admin
import org.javamoney.moneta.Money
import javax.persistence.Entity


@Entity
class AdminPayment : Payment {

    constructor(): super()

    constructor(amount: Money, admin: Admin) : super(amount, admin)

    override fun getPaymentMethod() = "Payment added by Admin"
}
