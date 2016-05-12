package backend.model.challenges

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.challenges.ChallengeStatus.*
import backend.model.event.Team
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

    var status: ChallengeStatus = PROPOSED
        private set(value) {
            checkTransition(from = field, to = value)
            field = value
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

    constructor(sponsor: Sponsor, team: Team, amount: Money) {
        this.sponsor = sponsor
        this.team = team
        this.amount = amount
    }

    constructor(unregisteredSponsor: UnregisteredSponsor, team: Team, amount: Money) {
        this.unregisteredSponsor = unregisteredSponsor
        this.team = team
        this.amount = amount
        this.status = ACCEPTED
    }

    @Throws
    private fun checkTransition(from: ChallengeStatus, to: ChallengeStatus) {
        val allowedTransitions = setOf(
                (PROPOSED to ACCEPTED),
                (PROPOSED to REJECTED),
                (ACCEPTED to WITH_PROOF),
                (ACCEPTED to REJECTED),
                (REJECTED to ACCEPTED),
                (ACCEPTED to WITH_PROOF),
                (WITH_PROOF to PROOF_ACCEPTED),
                (WITH_PROOF to PROOF_REJECTED),
                (PROOF_REJECTED to PROOF_ACCEPTED))

        if (!allowedTransitions.contains((from to to))) {
            throw DomainException("Transition from $from to $to not allowed")
        }
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


}

