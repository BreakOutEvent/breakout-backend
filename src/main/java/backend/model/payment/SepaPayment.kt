package backend.model.payment

import backend.model.user.Admin
import backend.model.user.User
import org.javamoney.moneta.Money
import java.time.LocalDateTime
import javax.persistence.Entity


@Entity
class SepaPayment : Payment {

    constructor() : super()

    constructor(amount: Money, admin: User, fidorId: Long, date: LocalDateTime?) : super(amount, admin, fidorId, date)

    override fun getPaymentMethod() = "Payment added by Payment-Service, received through wire transfer"
}
