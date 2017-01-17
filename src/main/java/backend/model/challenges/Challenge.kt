package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.challenges.ChallengeStatus.*
import backend.model.event.Team
import backend.model.media.Media
import backend.model.media.MediaType.DOCUMENT
import backend.model.misc.EmailAddress
import backend.model.payment.SponsoringInvoice
import backend.model.posting.Posting
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import javax.persistence.*
import javax.persistence.CascadeType.ALL

@Entity
class Challenge : BasicEntity {

    @Column(columnDefinition = "TEXT")
    lateinit var description: String

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    lateinit var contract: Media

    var status: ChallengeStatus = PROPOSED
        private set (value) {
            checkTransition(from = field, to = value)
            field = value
        }

    private fun checkTransition(from: ChallengeStatus, to: ChallengeStatus) {
        if (from == to) return
        else if (unregisteredSponsor != null) checkTransitionForUnregisteredSponsor(from, to)
        else if (registeredSponsor != null) checkTransitionForRegisteredSponsor(from, to)
        else throw Exception("Sponsoring has neither Sponsor")
    }

    private fun checkTransitionForUnregisteredSponsor(from: ChallengeStatus, to: ChallengeStatus) {
        val transitions = listOf(
                (PROPOSED to ACCEPTED),
                (PROPOSED to REJECTED),
                (PROPOSED to WITH_PROOF),
                (ACCEPTED to REJECTED),
                (ACCEPTED to WITHDRAWN),
                (REJECTED to ACCEPTED),
                (ACCEPTED to WITH_PROOF),
                (WITH_PROOF to PROOF_ACCEPTED),
                (WITH_PROOF to PROOF_REJECTED),
                (PROOF_REJECTED to PROOF_ACCEPTED))

        if (!transitions.contains(from to to)) {
            throw DomainException("Transition from $from to $to for status not allowed")
        }
    }

    private fun checkTransitionForRegisteredSponsor(from: ChallengeStatus, to: ChallengeStatus) {
        val transitions = listOf(
                (PROPOSED to ACCEPTED),
                (PROPOSED to REJECTED),
                (PROPOSED to WITH_PROOF),
                (PROPOSED to WITHDRAWN),
                (ACCEPTED to REJECTED),
                (ACCEPTED to WITHDRAWN),
                (REJECTED to ACCEPTED),
                (REJECTED to WITHDRAWN),
                (ACCEPTED to WITH_PROOF),
                (WITH_PROOF to PROOF_ACCEPTED),
                (WITH_PROOF to PROOF_REJECTED),
                (PROOF_REJECTED to PROOF_ACCEPTED))

        if (!transitions.contains(from to to)) {
            throw DomainException("Transition from $from to $to for status not allowed")
        }
    }

    lateinit var amount: Money
        private set

    // TOOD: Remove after using Join Table
    @Deprecated("Used for PreRemove on Sponsor. A Challenge should never exist without a sponsor")
    fun removeSponsor() {
        this.registeredSponsor = null
        this.unregisteredSponsor = null
    }

    fun getSponsor(): ISponsor {
        return this.unregisteredSponsor ?: this.registeredSponsor!!
    }

    @ManyToOne
    var team: Team? = null

    @ManyToOne
    var invoice: SponsoringInvoice? = null

    @ManyToOne
    private var registeredSponsor: Sponsor? = null
        set(value) {
            if (unregisteredSponsor != null) {
                throw DomainException("Can't add registeredSponsor to challenge with unregistered sponsor")
            } else {
                field = value
            }
        }

    @OneToOne(mappedBy = "challenge")
    var proof: Posting? = null

    @Embedded
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

    constructor(sponsor: ISponsor, team: Team, amount: Money, description: String) {
        when (sponsor) {
            is UnregisteredSponsor -> {
                this.unregisteredSponsor = sponsor
                this.status = ACCEPTED
            }
            is Sponsor -> this.registeredSponsor = sponsor
            else -> throw Exception("sponsor: ISponsor does not Sponsor or UnregisteredSponsor")
        }

        this.team = team
        this.amount = amount
        this.description = description
        this.contract = Media(DOCUMENT)
    }

    fun accept() {
        this.status = ACCEPTED
    }

    fun reject() {
        this.status = REJECTED
    }

    fun addProof(proof: Posting) {
        this.status = WITH_PROOF
        this.proof = proof
    }

    fun acceptProof() {
        this.status = PROOF_ACCEPTED
    }

    fun rejectProof() {
        this.status = PROOF_REJECTED
    }

    fun withdraw() {
        this.status = WITHDRAWN
    }

    @Suppress("UNUSED") //Used by Spring @PreAuthorize
    fun checkWithdrawPermissions(username: String): Boolean {
        if (this.unregisteredSponsor != null) {
            return this.team!!.isMember(username)
        } else if (this.registeredSponsor != null) {
            return EmailAddress(this.registeredSponsor!!.email) == EmailAddress(username)
        } else throw Exception("Error checking withdrawal permissions")
    }

    fun hasRegisteredSponsor(): Boolean {
        return registeredSponsor != null
    }
}

