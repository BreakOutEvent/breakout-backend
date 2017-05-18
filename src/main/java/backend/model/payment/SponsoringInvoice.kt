package backend.model.payment

import backend.exceptions.DomainException
import backend.model.challenges.Challenge
import backend.model.event.Team
import backend.model.sponsoring.Sponsoring
import org.javamoney.moneta.Money
import javax.persistence.*

@Entity
class SponsoringInvoice : Invoice {

    @OneToOne(fetch = FetchType.LAZY)
    var team: Team? = null

    @OneToMany(cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST))
    var sponsorings: List<Sponsoring> = listOf()

    @OneToMany(cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST))
    var challenges: List<Challenge> = listOf()

    var subject: String? = null

    var company: String? = null

    var firstname: String? = null

    var lastname: String? = null

    private constructor() : super()

    constructor(team: Team, amount: Money, subject: String, sponsorings: List<Sponsoring>, challenges: List<Challenge>) : super(amount) {
        this.team = team
        this.subject = subject
        this.sponsorings = sponsorings
        this.challenges = challenges
    }

    constructor(team: Team, amount: Money, company: String, firstname: String, lastname: String) : super(amount) {
        this.team = team
        this.company = company
        this.firstname = firstname
        this.lastname = lastname
    }

    override fun checkPaymentEligability(payment: Payment) {
        if (!team!!.isFull()) throw DomainException("Payments can only be added to teams which already have two members")
    }

    override fun generatePurposeOfTransfer(): String {
        TODO()
    }
}
