package backend.model.payment

import backend.model.user.Admin
import org.javamoney.moneta.Money
import java.time.LocalDateTime
import javax.persistence.Entity


@Entity
class AdminPayment : Payment {

    constructor() : super()

    constructor(amount: Money, admin: Admin, fidorId: Long? = null, date: LocalDateTime? = null) : super(amount, admin, fidorId, date)

    override fun getPaymentMethod() = "Payment added by Admin"
}
