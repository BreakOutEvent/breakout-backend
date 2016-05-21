package backend.model.sponsoring

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.misc.EmailAddress
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
        if (unregisteredSponsor != null) checkTransitionForUnregisteredSponsor(from, to)
        else if (sponsor != null) checkTransitionForRegisteredSponsor(from, to)
        else throw Exception("Sponsoring has neither Sponsor")
    }

    private fun checkTransitionForUnregisteredSponsor(from: SponsoringStatus, to: SponsoringStatus) {
        val transitions = listOf(
                PROPOSED to ACCEPTED,
                ACCEPTED to WITHDRAWN,
                ACCEPTED to PAYED)
        if (!transitions.contains(from to to)) {
            throw DomainException("Transition from $from to $to for status not allowed")
        }
    }

    private fun checkTransitionForRegisteredSponsor(from: SponsoringStatus, to: SponsoringStatus) {
        val transitions = listOf(
                PROPOSED to ACCEPTED,
                PROPOSED to REJECTED,
                PROPOSED to WITHDRAWN,
                ACCEPTED to WITHDRAWN,
                ACCEPTED to PAYED)

        if (!transitions.contains(from to to)) {
            throw DomainException("Transition from $from to $to for status not allowed")
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
        this.status = WITHDRAWN
    }

    fun hasRegisteredSponsor(): Boolean {
        return sponsor != null
    }

    @Suppress("UNUSED") //Used by Spring @PreAuthorize
    fun checkWithdrawPermissions(username: String): Boolean {
        if (this.unregisteredSponsor != null) {
            return this.team!!.isMember(username)
        } else if (this.sponsor != null) {
            return EmailAddress(this.sponsor!!.email) == EmailAddress(username)
        } else throw Exception("Error checking withdrawal permissions")
    }

    private fun calculateAmount(): Money {
        val kilometers = team!!.getMaximumLinearDistanceKM()
        val amountPerKmAsBigDecimal = amountPerKm.numberStripped
        val total = amountPerKmAsBigDecimal.multiply(BigDecimal.valueOf(kilometers))

        return Money.of(total, "EUR")
    }
}
