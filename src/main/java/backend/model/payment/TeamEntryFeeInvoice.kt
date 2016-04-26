package backend.model.payment

import backend.exceptions.DomainException
import backend.model.event.Team
import org.javamoney.moneta.Money
import javax.persistence.Entity
import javax.persistence.OneToOne

@Entity
class TeamEntryFeeInvoice : Invoice {

    @OneToOne
    var team: Team? = null

    private constructor() : super()

    constructor(team: Team, amount: Money) : super(amount) {
        this.team = team
        this.team!!.invoice = this
    }

    override fun checkPaymentEligability(payment: Payment) {
        if (payment !is AdminPayment) throw DomainException("Currently only payments via admins can be added to team invoices")
        if (!isHalfOrFullAmount(payment.amount)) throw DomainException("Only the half or full amount of a payment can be added!")
        if (!team!!.isFull()) throw DomainException("Payments can only be added to teams which already have two members")
    }

    private fun isHalfOrFullAmount(money: Money): Boolean {
        val isHalfAmount = money.isEqualTo(this.amount.divide(2))
        val isFullAmount = money.isEqualTo(this.amount)
        return (isHalfAmount || isFullAmount)
    }
}
