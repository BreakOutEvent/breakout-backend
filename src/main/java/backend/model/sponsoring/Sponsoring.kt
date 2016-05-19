package backend.model.sponsoring

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class Sponsoring : BasicEntity {

    var status: SponsoringStatus = PROPOSED
        private set (value) {
            checkTransition(from = field, to = value)
            field = value
        }

    private fun checkTransition(from: SponsoringStatus, to: SponsoringStatus) {

        val transitions = listOf(
                PROPOSED to ACCEPTED,
                PROPOSED to REJECTED,
                PROPOSED to WITHDRAWN,
                ACCEPTED to REJECTED,
                REJECTED to ACCEPTED)

        if (from == to) {
            return
        } else if (!transitions.contains(from to to)) {
            throw DomainException("Changing the status of a sponsoring from $from to $to is not allowed")
        }
    }

    lateinit var amountPerKm: Money
        private set

    @Column(name = "sponsoring_limit")
    lateinit var limit: Money
        private set

    @ManyToOne
    var team: Team? = null

    @ManyToOne
    var sponsor: Sponsor? = null

    @Embedded
    var unregisteredSponsor: UnregisteredSponsor? = null

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor() : super()

    constructor(sponsor: Sponsor, team: Team, amountPerKm: Money, limit: Money) {
        this.sponsor = sponsor
        this.team = team
        this.amountPerKm = amountPerKm
        this.limit = limit
    }

    constructor(unregisteredSponsor: UnregisteredSponsor, team: Team, amountPerKm: Money, limit: Money) {
        this.unregisteredSponsor = unregisteredSponsor
        this.team = team
        this.amountPerKm = amountPerKm
        this.limit = limit
        this.status = ACCEPTED
    }

    fun calculateRaisedAmount(): Money {
        if (reachedLimit()) return limit
        else return calculateAmount()
    }

    fun reachedLimit(): Boolean {
        return calculateAmount().isGreaterThan(limit)
    }

    fun accept() {
        this.status = ACCEPTED
    }

    fun reject() {
        this.status = REJECTED
    }

    fun withdraw() {
        if (this.status == PROPOSED) {
            this.status = WITHDRAWN
        } else {
            throw DomainException("A challenge can only be withdrawn when its current status is proposed")
        }
    }

    private fun calculateAmount(): Money {
        val kilometers = team!!.getMaximumLinearDistanceKM()
        val amountPerKmAsBigDecimal = amountPerKm.numberStripped
        val total = amountPerKmAsBigDecimal.multiply(BigDecimal.valueOf(kilometers))

        return Money.of(total, "EUR")
    }
}
