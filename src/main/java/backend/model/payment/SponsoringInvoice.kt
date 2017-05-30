package backend.model.payment

import backend.exceptions.DomainException
import backend.model.challenges.Challenge
import backend.model.event.Event
import backend.model.event.Team
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.UnregisteredSponsor
import backend.model.user.Sponsor
import backend.util.euroOf
import org.javamoney.moneta.Money
import org.springframework.format.number.money.MonetaryAmountFormatter
import java.util.*
import javax.persistence.*

@Entity
class SponsoringInvoice : Invoice {

    @OneToMany(cascade = arrayOf(CascadeType.MERGE))
    var sponsorings: List<Sponsoring> = listOf()
        private set

    @OneToMany(cascade = arrayOf(CascadeType.MERGE))
    var challenges: List<Challenge> = listOf()
        private set

    @ManyToOne
    var event: Event? = null

    @ManyToOne
    public var unregisteredSponsor: UnregisteredSponsor? = null

    @ManyToOne
    public var registeredSponsor: Sponsor? = null

    public var initialVersionSent: Boolean = false

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

    // TODO: Add unit test, so that only those with this specific event are added!
    constructor(sponsor: ISponsor, event: Event) : super(euroOf(0.0)) {
        this.challenges = sponsor.challenges.filter { it.belongsToEvent(event.id!!) }
        this.sponsorings = sponsor.sponsorings.filter { it.belongsToEvent(event.id!!) }
        this.amount = this.challenges.billableAmount().add(this.sponsorings.billableAmount())
        this.sponsor = sponsor
        this.event = event
        this.purposeOfTransfer = generatePurposeOfTransfer()
    }

    private constructor() : super()

    override fun checkPaymentEligability(payment: Payment) {
        if (!team!!.isFull()) throw DomainException("Payments can only be added to teams which already have two members")
    }

    override fun generatePurposeOfTransfer(): String {
        this.purposeOfTransferCode = generateRandomPurposeOfTransferCode()
        this.purposeOfTransfer = "Spende $purposeOfTransferCode-BREAKOUT-${sponsor.lastname}"

        return this.purposeOfTransfer!!
    }

    fun toEmailOverview(): String {
        return """
        |<b>Challenges</b>
        |${this.challenges.toEmailListing()}
        |<b>Kilometerspenden / Donations per km</b>
        |${this.sponsorings.toEmailListing()}
        |
        |<b>Total:</b> ${amount.display()}
        |<b>Bereits bezahlt:</b> ${amountOfCurrentPayments().display()}
        """.trimMargin("|")
    }

    @JvmName("sponsoringToEmailListing")
    private fun List<Sponsoring>.toEmailListing(): String {
        return this.map { it.toEmailListing() }.foldRight("") { acc, s -> "$acc\n$s" }
    }

    private fun Sponsoring.toEmailListing(): String {
        return "<b>Team-ID</b> ${this.team?.id} <b>Teamname</b> ${this.team?.name} <b>Status</b> ${this.status} <b>Betrag pro km</b> ${this.amountPerKm.display()} <b>Limit</b> ${this.limit.display()} <b>Gereiste KM</b> ${this.team?.getCurrentDistance()} <b>Spendenversprechen</b> ${this.billableAmount().display()}"
    }

    @JvmName("challengeToEmailListing")
    private fun List<Challenge>.toEmailListing(): String {
        return this.map { it.toEmailListing() }.foldRight("") { acc, s -> "$acc\n$s" }
    }

    private fun Challenge.toEmailListing(): String {
        println()
        return "<b>Team-ID</b> ${this.team?.id} <b>Teamname</b> ${this.team?.name} <b>Beschreibung</b> ${this.description.take(50)}... <b>Challengebetrag</b> ${this.amount.display()} <b>Spendenversprechen</b> ${this.billableAmount().display()}"
    }

    fun getContactEmails(): List<EmailAddress> {
        return when (sponsor) {
            is UnregisteredSponsor -> {
                val fromChallenges = this.challenges.flatMap { it.team?.members?.map { EmailAddress(it.email) } ?: listOf() }
                val fromSponsorings = this.sponsorings.flatMap { it.team?.members?.map { EmailAddress(it.email) } ?: listOf() }
                var fromUnregistered: Iterable<EmailAddress>

                if(this.unregisteredSponsor?.email != null) {
                    try {
                        fromUnregistered = listOf(EmailAddress(this.unregisteredSponsor!!.email!!))
                    } catch (e: Exception) {
                        fromUnregistered = listOf<EmailAddress>()
                    }

                } else {
                    fromUnregistered = listOf<EmailAddress>()
                }

                val total = fromChallenges
                        .union(fromSponsorings)
                        .union(fromUnregistered)
                        .distinct()

                if(total.size > 3) throw Exception("There should be at max 3 emails to contact per invoice")
                return total
            }
            is Sponsor -> listOf(EmailAddress(registeredSponsor!!.email))
            else -> throw Exception("No sponsor email found for invoice $id")
        }
    }
}

fun Money.display(): String {
    return MonetaryAmountFormatter().print(this, Locale.GERMANY)
}
