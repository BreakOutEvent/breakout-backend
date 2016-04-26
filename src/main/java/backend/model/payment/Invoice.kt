package backend.model.payment

import backend.model.BasicEntity
import org.javamoney.moneta.Money
import javax.persistence.*
import javax.persistence.CascadeType.MERGE
import javax.persistence.CascadeType.PERSIST

@Entity
abstract class Invoice : BasicEntity {

    @OneToMany(cascade = arrayOf(MERGE, PERSIST), mappedBy = "invoice", orphanRemoval = true)
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
        payment.invoice = this
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

    @PreRemove
    fun preRemove() {
        this.payments.forEach { it.invoice = null }
        payments.clear()
    }
}
