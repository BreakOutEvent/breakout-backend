package backend.model.payment

import backend.model.user.Admin
import org.javamoney.moneta.Money
import javax.persistence.Entity


@Entity
class SepaPayment : Payment {

    constructor() : super()

    constructor(amount: Money, admin: Admin, fidorId: Long) : super(amount, admin, fidorId)

    override fun getPaymentMethod() = "Payment added by Payment-Service, received through wire transfer"
}
