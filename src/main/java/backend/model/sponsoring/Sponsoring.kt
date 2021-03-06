package backend.model.sponsoring

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Team
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.payment.Billable
import backend.model.sponsoring.SponsoringStatus.*
import backend.model.user.Sponsor
import backend.util.euroOf
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.*
import javax.persistence.CascadeType.PERSIST

@Entity
class Sponsoring : BasicEntity, Billable {

    @Transient
    private val logger: Logger = LoggerFactory.getLogger(Sponsoring::class.java)

    @OneToOne(cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var contract: Media? = null

    var status: SponsoringStatus = ACCEPTED
        private set (value) {
            checkTransition(from = field, to = value)
            field = value
        }

    lateinit var amountPerKm: Money
        private set

    @Column(name = "sponsoring_limit")
    lateinit var limit: Money
        private set

    @ManyToOne
    var team: Team? = null

    @ManyToOne(cascade = [PERSIST])
    private var unregisteredSponsor: UnregisteredSponsor? = null

    @ManyToOne
    private var registeredSponsor: Sponsor? = null

    var sponsor: ISponsor?
        get() = this.unregisteredSponsor as? ISponsor ?: this.registeredSponsor
        private set(value) {}

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor() : super()

    constructor(sponsor: Sponsor, team: Team, amountPerKm: Money, limit: Money) : this() {
        this.registeredSponsor = sponsor
        this.team = team
        this.amountPerKm = amountPerKm
        this.limit = limit
        this.contract = null
        this.sponsor?.sponsorings?.add(this)
    }

    constructor(unregisteredSponsor: UnregisteredSponsor, team: Team, amountPerKm: Money, limit: Money) : this() {
        this.unregisteredSponsor = unregisteredSponsor
        this.team = team
        this.amountPerKm = amountPerKm
        this.limit = limit
        this.status = ACCEPTED
        this.contract = null
        this.sponsor?.sponsorings?.add(this)
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
        return registeredSponsor != null
    }

    // TOOD: Remove after using Join Table
    @Deprecated("Used for PreRemove on Sponsor. A Sponsoring should never exist without a sponsor")
    fun removeSponsors() {
        this.unregisteredSponsor = null
        this.registeredSponsor = null
    }

    @Suppress("UNUSED") //Used by Spring @PreAuthorize
    fun checkWithdrawPermissions(username: String): Boolean {
        return when {
            this.unregisteredSponsor != null -> this.team!!.isMember(username)
            this.registeredSponsor != null -> EmailAddress(this.registeredSponsor!!.email) == EmailAddress(username)
            else -> throw Exception("Error checking withdrawal permissions")
        }
    }

    private fun checkTransition(from: SponsoringStatus, to: SponsoringStatus) {
        when {
            from == to -> return
            unregisteredSponsor != null -> checkTransitionForUnregisteredSponsor(from, to)
            registeredSponsor != null -> checkTransitionForRegisteredSponsor(from, to)
            else -> throw Exception("Sponsoring has neither Sponsor")
        }
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
                REJECTED to WITHDRAWN,
                ACCEPTED to WITHDRAWN,
                ACCEPTED to REJECTED,
                ACCEPTED to PAYED)

        if (!transitions.contains(from to to)) {
            throw DomainException("Transition from $from to $to for status not allowed")
        }
    }

    override fun billableAmount(): Money {

        val distance: Double = team?.getCurrentDistance() ?: run {
            logger.warn("No team for sponsoring $id found. Using 0.0 for currentDistance")
            return@run 0.0
        }

        val raisedSum = when (this.status) {
            ACCEPTED -> amountPerKm.multiply(distance)
            PAYED -> amountPerKm.multiply(distance)
            else -> euroOf(0.0)
        }

        return if (raisedSum <= limit) {
            raisedSum
        } else {
            limit
        }
    }

    fun belongsToEvent(eventId: Long): Boolean {
        return this.team?.event?.id == eventId
    }

    fun removeSponsor() {
        unregisteredSponsor = null
        registeredSponsor = null
    }
}
