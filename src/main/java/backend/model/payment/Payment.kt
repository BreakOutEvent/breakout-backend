package backend.model.payment

import backend.model.BasicEntity
import backend.model.user.User
import backend.model.user.UserCore
import org.javamoney.moneta.Money
import javax.persistence.*

@Entity
abstract class Payment: BasicEntity {

    lateinit var amount: Money
        private set

    @ManyToOne
    private lateinit var user: UserCore

    constructor()

    constructor(amount: Money, user: User) {
        this.amount = amount
        this.user = user.core
    }

    abstract fun getPaymentMethod(): String

    fun user(): User {
        return this.user
    }
}
