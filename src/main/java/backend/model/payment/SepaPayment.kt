package backend.model.payment

import backend.model.user.Admin
import org.javamoney.moneta.Money
import java.time.LocalDateTime
import javax.persistence.Entity


@Entity
class SepaPayment : Payment {

    constructor() : super()

    constructor(amount: Money, admin: Admin, fidorId: Long, date: LocalDateTime?) : super(amount, admin, fidorId, date)

    override fun getPaymentMethod() = "Payment added by Payment-Service, received through wire transfer"
}
