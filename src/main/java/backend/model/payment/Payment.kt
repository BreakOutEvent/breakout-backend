package backend.model.payment

import backend.model.BasicEntity
import backend.model.user.User
import backend.model.user.UserAccount
import org.javamoney.moneta.Money
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.PreRemove

@Entity
abstract class Payment : BasicEntity {

    lateinit var amount: Money
        private set

    @ManyToOne
    var user: UserAccount? = null

    @ManyToOne
    var invoice: Invoice? = null

    constructor()

    constructor(amount: Money, user: User) {
        this.amount = amount
        this.user = user.account
    }

    abstract fun getPaymentMethod(): String

    fun user(): User? {
        return this.user
    }

    @PreRemove
    fun preRemove() {
        this.user?.payments?.remove(this)
        this.user = null
    }
}
