package backend.model.event

import backend.model.Blockable
import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeStatus
import backend.model.location.Location
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.payment.TeamEntryFeeInvoice
import backend.model.payment.billableAmount
import backend.model.payment.display
import backend.model.sponsoring.Sponsoring
import backend.model.user.Participant
import org.hibernate.annotations.Formula
import org.javamoney.moneta.Money
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.persistence.CascadeType.REMOVE
import javax.persistence.FetchType.LAZY

@Entity
class Team : BasicEntity, Blockable {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(creator: Participant, name: String, description: String, event: Event, profilePic: Media?, postaddress: String? = null) : this() {
        this.event = event
        this.event.teams.add(this)
        this.addMember(creator)
        this.name = name
        this.description = description
        this.profilePic = profilePic
        this.invoice = event.teamFee?.let { fee ->
            if (fee.isPositive) {
                TeamEntryFeeInvoice(this, fee)
            } else {
                null
            }
        }
        this.postaddress = postaddress
    }

    var hasStarted: Boolean = false

    var asleep: Boolean = false

    lateinit var name: String

    @ManyToOne(fetch = LAZY)
    lateinit var event: Event

    @Column(columnDefinition = "TEXT")
    lateinit var description: String

    var postaddress: String? = null

    @OneToMany(cascade = [ALL], mappedBy = "team", orphanRemoval = true)
    private var invitations: MutableList<Invitation> = ArrayList()

    @OneToOne(cascade = [ALL], orphanRemoval = true, fetch = LAZY)
    var profilePic: Media? = null

    @ManyToMany
    val members: MutableSet<Participant> = HashSet()

    @OneToMany(cascade = [REMOVE], mappedBy = "team", orphanRemoval = true)
    val locations: MutableList<Location> = ArrayList()

    // TODO: Performance: Should probably be removed and used with invoiceService
    @OneToOne(cascade = [ALL], orphanRemoval = true, mappedBy = "team", fetch = LAZY)
    var invoice: TeamEntryFeeInvoice? = null

    @OneToMany(cascade = [ALL], orphanRemoval = true, mappedBy = "team")
    var sponsoring: MutableList<Sponsoring> = ArrayList()

    @OneToMany(cascade = [ALL], orphanRemoval = true, mappedBy = "team")
    var challenges: MutableList<Challenge> = ArrayList()

    @Formula("(select max(l.distance) from location l inner join team t on (l.team_id = t.id) where l.is_during_event and t.id = id)")
    private var currentDistance: Double? = 0.0

    private fun addMember(participant: Participant) {

        if (participant.participatedAtEvent(event)) {
            throw DomainException("A participant can't join more than one team at the same event")
        }

        if (this.isFull()) throw DomainException("This team already has two members")

        members.add(participant)
        participant.setCurrentTeam(this)
    }

    @Throws
    fun join(participant: Participant): Set<Participant> {

        val inviteeEmail = EmailAddress(participant.email)
        if (!isInvited(inviteeEmail)) {
            throw DomainException("${participant.email} can't join team because he is not invited")
        } else if (isFull()) {
            throw DomainException("${participant.email} can't join team because this team is already full")
        } else {
            addMember(participant)
            return this.members
        }
    }

    @Throws
    fun invite(email: EmailAddress): Invitation {
        if (isInvited(email)) throw DomainException("User $email already is invited to this team")
        val invitation = Invitation(email, this)
        this.invitations.add(invitation)
        return invitation
    }

    fun isInvited(email: EmailAddress): Boolean {
        return this.invitations.map { it.invitee }.contains(email)
    }

    // This is used by a @PreAuthorize statement
    // which does not get recognized by the compiler
    @Suppress("UNUSED")
    fun isMember(username: String): Boolean {
        return this.members.map(Participant::email).contains(username)
    }

    fun isMember(participant: Participant): Boolean {
        return this.members.any { it.account == participant.account }
    }

    fun isFull(): Boolean {
        return this.members.count() >= 2
    }

    fun leave(participant: Participant) {
        if (!this.isMember(participant)) throw DomainException("Can't leave team because user never was a part of it")

        // TODO: Check whether we still want this behaviour!
        if (this.isFull()) throw DomainException("Can't leave team because it is already full")
        this.invitations.forEach { it.team = null }
        this.invitations.clear()

        this.members.filter { it == participant }
                .forEach { it.removeTeam(this) }
    }

    @PreRemove
    fun preRemove() {
        this.members.forEach { it.clearAllTeams() }
        this.members.clear()

        this.locations.forEach { it.team = null }
        this.locations.clear()

        this.invoice?.team = null
        this.invoice = null

        this.invitations.forEach { it.team = null }
        this.invitations.clear()

        this.sponsoring.forEach { it.team = null }
        this.sponsoring.clear()

        this.challenges.forEach { it.team = null }
        this.challenges.clear()
    }

    fun raisedAmountFromChallenges(): Money {
        return this.challenges.billableAmount()
    }

    fun raisedAmountFromSponsorings(): Money {
        return this.sponsoring.billableAmount()
    }

    @Deprecated("The naming on this is bad as the current distance is actually the farthest distance during the event. This will be renamed")
    fun getCurrentDistance(): Double {
        return currentDistance ?: 0.0
    }

    fun toEmailOverview(): String {
        val challenges = this.challenges.filter { it.status == ChallengeStatus.PROPOSED || it.status == ChallengeStatus.WITH_PROOF }
        return """
        |<b>Challenges</b>
        |${challenges.toEmailListing()}
        |<b>Kilometerspenden / Donations per km</b>
        |${this.sponsoring.toEmailListing()}
        """.trimMargin("|")
    }

    @JvmName("sponsoringToEmailListing")
    private fun List<Sponsoring>.toEmailListing(): String {
        return this.map { it.toEmailListing() }.foldRight("") { acc, s -> "$acc\n$s" }
    }

    private fun Sponsoring.toEmailListing(): String {
        return "<b>Name</b> ${this.sponsor?.firstname} ${this.sponsor?.lastname} <b>Status</b> ${this.status} <b>Betrag pro km</b> ${this.amountPerKm.display()} <b>Limit</b> ${this.limit.display()} <b>Gereiste KM</b> ${this.team?.getCurrentDistance()} <b>Spendenversprechen</b> ${this.billableAmount().display()}"
    }

    @JvmName("challengeToEmailListing")
    private fun List<Challenge>.toEmailListing(): String {
        return this.map { it.toEmailListing() }.foldRight("") { acc, s -> "$acc\n$s" }
    }

    private fun Challenge.toEmailListing(): String {
        println()
        return "<b>Name</b> ${this.sponsor?.firstname} ${this.sponsor?.lastname} <b>Beschreibung</b> ${this.description.take(50)}... <b>Challengebetrag</b> ${this.amount.display()} <b>Spendenversprechen</b> ${this.billableAmount().display()}"
    }

    override fun isBlockedBy(userId: Long?): Boolean {
        return this.members.fold(true) { acc, participant -> acc && participant.isBlockedBy(userId) }
    }

}
