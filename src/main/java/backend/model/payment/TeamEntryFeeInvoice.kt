package backend.model.payment

import backend.model.event.Team
import backend.model.user.Participant
import org.javamoney.moneta.Money

class TeamEntryFeeInvoice : Invoice {

    var team: Team
        private set

    constructor(team: Team, amount: Money) : super(amount) {
        this.team = team
    }

    override fun isPaymentEligable(payment: Payment): Boolean {
        val participant = payment.user.getRole(Participant::class)

        return when {
            participant == null -> false
            !team.isMember(participant) -> false
            !payment.amount.isEqualTo(this.amount.divide(2)) -> false
            !team.isFull() -> false
            else -> true
        }
    }
}
