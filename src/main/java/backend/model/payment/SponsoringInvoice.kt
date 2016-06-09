package backend.model.payment

import backend.exceptions.DomainException
import backend.model.challenges.Challenge
import backend.model.event.Team
import backend.model.sponsoring.Sponsoring
import org.javamoney.moneta.Money
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
class SponsoringInvoice : Invoice {

    @OneToOne
    var team: Team? = null

    @OneToMany(mappedBy = "invoice", cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST))
    var sponsorings: List<Sponsoring> = listOf()

    @OneToMany(mappedBy = "invoice", cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST))
    var challenges: List<Challenge> = listOf()

    var subject: String? = null

    private constructor() : super()

    constructor(team: Team, amount: Money, subject: String, sponsorings: List<Sponsoring>, challenges: List<Challenge>) : super(amount) {
        this.team = team
        this.subject = subject
        this.sponsorings = sponsorings
        this.challenges = challenges
    }

    override fun checkPaymentEligability(payment: Payment) {
        if (!team!!.isFull()) throw DomainException("Payments can only be added to teams which already have two members")
    }
}
