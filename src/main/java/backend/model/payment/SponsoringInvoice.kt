package backend.model.payment

import backend.exceptions.DomainException
import backend.model.challenges.Challenge
import backend.model.event.Team
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import javax.persistence.*

@Entity
class SponsoringInvoice : Invoice {

    @OneToMany(cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST))
    var sponsorings: List<Sponsoring> = listOf()
        private set

    @OneToMany(cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST))
    var challenges: List<Challenge> = listOf()
        private set

    @OneToOne
    private var unregisteredSponsor: UnregisteredSponsor? = null

    @OneToOne
    private var registeredSponsor: Sponsor? = null

    var sponsor: ISponsor
        private set(value) {
            when (value) {
                is UnregisteredSponsor -> this.unregisteredSponsor = value
                is Sponsor -> this.registeredSponsor = value
                else -> throw Exception("Unsupported implementation of ISponsor found: ${value::class}")
            }
        }
        get() {
            this.unregisteredSponsor?.let { return it }
            this.registeredSponsor?.let { return it }
            throw Exception("Neither registered nor unregistered sponsor are found in invoice $id")
        }

    /*
     * Begin deprecated properties
     * Those properties where used in previous versions to store informations about sponsors
     * as they were built on a per-team basis. Current invoices are built on a per sponsor basis, which
     * has a @OneToOne sponsor built in. Those properties are needed to access the legacy information, but should
     * not be used actively anymore.
     */
    @Deprecated("Older invoices used this as their subject. This property allows to access this information")
    var subject: String? = null

    @Deprecated("Older invoices stored the sponsor company 'inline'. This property allows to access this information")
    var company: String? = null

    @Deprecated("Older invoices stored the sponsor company 'inline'. This property allows to access this information")
    var firstname: String? = null

    @Deprecated("Older invoices stored the sponsor company 'inline'. This property allows to access this information")
    var lastname: String? = null

    @Deprecated("Older invoices were built on a per team basis. This property allows to access this information")
    @OneToOne(fetch = FetchType.LAZY)
    var team: Team? = null

    @Deprecated("Older invoices were built on a per team basis. This constructor allowed one to do so")
    constructor(team: Team, amount: Money, subject: String, sponsorings: List<Sponsoring>, challenges: List<Challenge>) : super(amount) {
        this.team = team
        this.subject = subject
        this.sponsorings = sponsorings
        this.challenges = challenges
    }

    @Deprecated("Older invoices were built on a per team basis. This constructor allowed one to do so")
    constructor(team: Team, amount: Money, company: String, firstname: String, lastname: String) : super(amount) {
        this.team = team
        this.company = company
        this.firstname = firstname
        this.lastname = lastname
    }
    /*
     * End deprecated properties
     */

    constructor(sponsor: ISponsor) : super(sponsor.challenges.billableAmount().add(sponsor.sponsorings.billableAmount())) {
        this.challenges = sponsor.challenges
        this.sponsorings = sponsor.sponsorings
        this.sponsor = sponsor
    }

    private constructor() : super()

    override fun checkPaymentEligability(payment: Payment) {
        if (!team!!.isFull()) throw DomainException("Payments can only be added to teams which already have two members")
    }

    override fun generatePurposeOfTransfer(): String {
        TODO()
    }
}
