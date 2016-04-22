package backend.model.payment

import backend.model.user.User
import com.braintreegateway.Transaction
import org.javamoney.moneta.Money
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.InheritanceType


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class BraintreePayment: Payment {

    lateinit var transactionId: String

    constructor(amount: Money, user: User, transaction: Transaction): super(amount, user) {
        this.transactionId = transaction.id
    }


    override fun getPaymentMethod(): String {
        return "Braintree"
    }

}
