package backend.model.sponsoring

import backend.model.BasicEntity
import backend.model.event.Team
import org.javamoney.moneta.Money
import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class Sponsoring : BasicEntity {

    lateinit var amountPerKm: Money
        private set

    @Column(name = "sponsoring_limit")
    lateinit var limit: Money
        private set

    @ManyToOne
    lateinit var team: Team
        private set


    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor() : super()

    constructor(team: Team, amountPerKm: Money, limit: Money) {
        this.team = team
        this.amountPerKm = amountPerKm
        this.limit = limit
    }

    fun calculateRaisedAmount(): Money {
        if (reachedLimit()) return limit
        else return calculateAmount()
    }

    fun reachedLimit(): Boolean {
        return calculateAmount().isGreaterThan(limit)
    }

    private fun calculateAmount(): Money {
        val kilometers = team.getMaximumLinearDistanceKM()
        val amountPerKmAsBigDecimal = amountPerKm.numberStripped
        val total = amountPerKmAsBigDecimal.multiply(BigDecimal.valueOf(kilometers))

        return Money.of(total, "EUR")
    }
}
