package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.challenges.ChallengeStatus.*
import backend.model.event.Team
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.payment.Billable
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.util.euroOf
import org.javamoney.moneta.Money
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.persistence.CascadeType.PERSIST

@Entity
class Challenge : BasicEntity, Billable {

    @Column(columnDefinition = "TEXT")
    lateinit var description: String

    @OneToOne(cascade = [ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var contract: Media? = null

    var status: ChallengeStatus = PROPOSED
        private set (value) {
            checkTransition(from = field, to = value)
            field = value
        }

    var fulfilledCount: Int = 0
        private set

    var maximumCount: Int? = 1

    private fun checkTransition(from: ChallengeStatus, to: ChallengeStatus) {
        when {
            from == to -> return
            unregisteredSponsor != null -> checkTransitionForUnregisteredSponsor(from, to)
            registeredSponsor != null -> checkTransitionForRegisteredSponsor(from, to)
            else -> throw Exception("Sponsoring has neither Sponsor")
        }
    }

    private fun checkTransitionForUnregisteredSponsor(from: ChallengeStatus, to: ChallengeStatus) {
        val transitions = listOf(
                (PROPOSED to REJECTED),
                (PROPOSED to WITH_PROOF),
                (PROPOSED to WITHDRAWN),
                (REJECTED to PROPOSED),
                (WITH_PROOF to PROPOSED),
                (WITH_PROOF to WITH_PROOF))

        if (!transitions.contains(from to to)) {
            throw DomainException("Transition from $from to $to for status not allowed")
        }
    }

    private fun checkTransitionForRegisteredSponsor(from: ChallengeStatus, to: ChallengeStatus) {
        val transitions = listOf(
                (PROPOSED to REJECTED),
                (PROPOSED to WITH_PROOF),
                (PROPOSED to WITHDRAWN),
                (REJECTED to WITHDRAWN),
                (REJECTED to PROPOSED),
                (WITH_PROOF to PROPOSED),
                (WITH_PROOF to WITH_PROOF))

        if (!transitions.contains(from to to)) {
            throw DomainException("Transition from $from to $to for status not allowed")
        }
    }

    lateinit var amount: Money
        private set

    fun removeSponsor() {
        this.registeredSponsor = null
        this.unregisteredSponsor = null
    }

    var sponsor: ISponsor?
        get() = this.unregisteredSponsor as? ISponsor ?: this.registeredSponsor
        private set(value) {}

    @ManyToOne(fetch = FetchType.LAZY)
    var team: Team? = null

    @ManyToOne(fetch = FetchType.LAZY)
    private var registeredSponsor: Sponsor? = null
        set(value) {
            if (unregisteredSponsor != null) {
                throw DomainException("Can't add registeredSponsor to challenge with unregistered sponsor")
            } else {
                field = value
            }
        }

    @ManyToOne(fetch = FetchType.LAZY, cascade = [PERSIST])
    private var unregisteredSponsor: UnregisteredSponsor? = null
        set(value) {
            if (registeredSponsor != null) {
                throw DomainException("Can't add unregistered sponsor to challenge with registeredSponsor")
            } else {
                field = value
            }
        }

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor() : super()

    constructor(sponsor: ISponsor, team: Team, amount: Money, description: String, maximumCount: Int? = 1) {
        when (sponsor) {
            is UnregisteredSponsor -> {
                this.unregisteredSponsor = sponsor
                sponsor.challenges.add(this)
                team.challenges.add(this)
            }
            is Sponsor -> {
                this.registeredSponsor = sponsor
                sponsor.challenges.add(this)
                team.challenges.add(this)
            }
            else -> throw Exception("sponsor: ISponsor does not Sponsor or UnregisteredSponsor")
        }

        this.team = team
        this.amount = amount
        this.description = description
        this.contract = null //TODO: how to handle contracts in future?
        this.maximumCount = maximumCount
    }

    fun reject() {
        this.status = REJECTED
    }

    fun addProof() {

        maximumCount?.let {
            if (it <= fulfilledCount) {
                throw DomainException("Challenge cannot be fulfilled more than $it times")
            }
        }

        this.status = WITH_PROOF
        this.fulfilledCount++
    }

    fun withdraw() {
        this.status = WITHDRAWN
    }

    fun takeBack() {
        this.status = PROPOSED
        this.fulfilledCount--
    }

    @Suppress("UNUSED") //Used by Spring @PreAuthorize
    fun checkWithdrawPermissions(username: String): Boolean {
        return when {
            this.unregisteredSponsor != null -> this.team!!.isMember(username)
            this.registeredSponsor != null -> EmailAddress(this.registeredSponsor!!.email) == EmailAddress(username)
            else -> throw Exception("Error checking withdrawal permissions")
        }
    }

    fun hasRegisteredSponsor(): Boolean {
        return registeredSponsor != null
    }

    override fun billableAmount(): Money {
        return when (status) {
            PROPOSED -> euroOf(0.0)
            WITHDRAWN -> euroOf(0.0)
            REJECTED -> euroOf(0.0)
            WITH_PROOF -> amount.multiply(fulfilledCount)
        }
    }

    fun belongsToEvent(eventId: Long): Boolean {
        return this.team?.event?.id == eventId
    }
}

