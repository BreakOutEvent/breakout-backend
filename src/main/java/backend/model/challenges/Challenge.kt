package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.challenges.ChallengeStatus.*
import backend.model.event.Team
import backend.model.misc.EmailAddress
import backend.model.posting.Posting
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
class Challenge : BasicEntity {

    lateinit var description: String

    var status: ChallengeStatus = PROPOSED
        private set (value) {
            checkTransition(from = field, to = value)
            field = value
        }

    private fun checkTransition(from: ChallengeStatus, to: ChallengeStatus) {
        if (unregisteredSponsor != null) checkTransitionForUnregisteredSponsor(from, to)
        else if (sponsor != null) checkTransitionForRegisteredSponsor(from, to)
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

    @ManyToOne
    var team: Team? = null

    @ManyToOne
    var sponsor: Sponsor? = null
        set(value) {
            if (unregisteredSponsor != null) {
                throw DomainException("Can't add sponsor to challenge with unregistered sponsor")
            } else {
                field = value
            }
        }

    @OneToOne
    var proof: Posting? = null

    @Embedded
    var unregisteredSponsor: UnregisteredSponsor? = null
        set(value) {
            if (sponsor != null) {
                throw DomainException("Can't unregistered sponsor to challenge with sponsor")
            } else {
                field = value
            }
        }

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor() : super()

    constructor(sponsor: Sponsor, team: Team, amount: Money, description: String) {
        this.sponsor = sponsor
        this.team = team
        this.amount = amount
        this.description = description
    }

    constructor(unregisteredSponsor: UnregisteredSponsor, team: Team, amount: Money, description: String) {
        this.unregisteredSponsor = unregisteredSponsor
        this.team = team
        this.amount = amount
        this.description = description
        this.status = ACCEPTED
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
        } else if (this.sponsor != null) {
            return EmailAddress(this.sponsor!!.email) == EmailAddress(username)
        } else throw Exception("Error checking withdrawal permissions")
    }

    fun hasRegisteredSponsor(): Boolean {
        return sponsor != null
    }
}

