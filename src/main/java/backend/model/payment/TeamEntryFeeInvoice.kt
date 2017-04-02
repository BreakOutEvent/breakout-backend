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
        val isAdminOrSepa = payment is AdminPayment || payment is SepaPayment

        if (exeedsTotalAmount(payment)) throw DomainException("This payment is not eligable because the total necessary amount of $amount would be exeeded")
        if (!isAdminOrSepa) throw DomainException("Currently only payments via admins or sepa can be added to team invoices")
        if (!isHalfOrFullAmount(payment.amount)) throw DomainException("Only the half or full amount of a payment can be added!")
        if (!team!!.isFull()) throw DomainException("Payments can only be added to teams which already have two members")
    }

    private fun exeedsTotalAmount(payment: Payment): Boolean {
        val after = payment.amount.add(this.amountOfCurrentPayments())
        return after > this.amount
    }

    private fun isHalfOrFullAmount(money: Money): Boolean {
        val isHalfAmount = money.isEqualTo(this.amount.divide(2))
        val isFullAmount = money.isEqualTo(this.amount)
        return (isHalfAmount || isFullAmount)
    }

    override fun generatePurposeOfTransfer(): String {
        val teamId = this.team?.id
                ?: throw DomainException("Can't generate purposeOfTransfer for unsaved team without id")
        val eventId = this.team?.event?.id
                ?: throw DomainException("Can't generate purposeOfTransfer for team without event")
        val invoiceId = this.id
                ?: throw DomainException("Can't generate purposeOfTransfer for unsaved invoice without id")

        this.purposeOfTransferCode = generateRandomPurposeOfTransferCode()
        this.purposeOfTransfer = "$purposeOfTransferCode-BREAKOUT$eventId-TEAM$teamId-INVOICE$invoiceId-ENTRYFREE"
        return this.purposeOfTransfer
    }
}
