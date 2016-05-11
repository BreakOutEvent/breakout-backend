package backend.model.sponsoring

import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.user.Address
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import java.math.BigDecimal
import javax.persistence.*

@Entity
class Sponsoring : BasicEntity {

    var status: SponsoringStatus = PROPOSED
        private set

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

    private fun calculateAmount(): Money {
        val kilometers = team!!.getMaximumLinearDistanceKM()
        val amountPerKmAsBigDecimal = amountPerKm.numberStripped
        val total = amountPerKmAsBigDecimal.multiply(BigDecimal.valueOf(kilometers))

        return Money.of(total, "EUR")
    }
}

enum class SponsoringStatus {
    PROPOSED, ACCEPTED, REJECTED
}

@Embeddable
class UnregisteredSponsor {

    lateinit var firstname: String
    lateinit var lastname: String
    lateinit var company: String
    lateinit var gender: String
    lateinit var url: String
    @Embedded lateinit var address: Address
    @Column(nullable = true) var isHidden: Boolean = false

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor()

    constructor(firstname: String,
                lastname: String,
                company: String,
                gender: String,
                url: String,
                address: Address,
                isHidden: Boolean = false) {

        this.firstname = firstname
        this.lastname = lastname
        this.company = company
        this.gender = gender
        this.url = url
        this.address = address
        this.isHidden = isHidden
    }
}
