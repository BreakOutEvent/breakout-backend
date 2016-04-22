package backend.model.payment

import backend.model.event.Team
import backend.model.user.Participant
import org.javamoney.moneta.Money
import javax.persistence.Entity
import javax.persistence.OneToOne

@Entity
class TeamEntryFeeInvoice : Invoice {

    @OneToOne
    var team: Team? = null

    private constructor(): super()

    constructor(team: Team, amount: Money) : super(amount) {
        this.team = team
        this.team!!.invoice = this
    }

    override fun isPaymentEligable(payment: Payment): Boolean {
        val participant = payment.user().getRole(Participant::class)

        return when {
            participant == null -> false
            !team!!.isMember(participant) -> false
            //TODO: Also allow half the payment
            !payment.amount.isEqualTo(this.amount.divide(2)) || payment.amount.isEqualTo(this.amount) -> false
            !team!!.isFull() -> false
            else -> true
        }
    }
}
