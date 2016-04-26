package backend.model.payment

import backend.model.BasicEntity
import org.javamoney.moneta.Money
import javax.persistence.Column
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany

@MappedSuperclass
abstract class Invoice : BasicEntity {

    @OneToMany
    private val payments: MutableList<Payment> = mutableListOf()

    @Column
    lateinit var amount: Money
        private set

    protected constructor()

    constructor(amount: Money) {
        this.amount = amount
    }

    fun addPayment(payment: Payment) {
        checkPaymentEligability(payment)
        this.payments.add(payment)
    }

    fun isFullyPaid(): Boolean {
        return this.amountOfCurrentPayments().isGreaterThanOrEqualTo(this.amount)
    }

    fun amountOfCurrentPayments(): Money {
        return this.payments.map { it.amount }.reduce { a, b -> a.add(b) }
    }

    fun getPayments(): Iterable<Payment> {
        val paymentsAsImmutableList: List<Payment> = this.payments
        return paymentsAsImmutableList
    }

    @Throws
    abstract fun checkPaymentEligability(payment: Payment)
}
