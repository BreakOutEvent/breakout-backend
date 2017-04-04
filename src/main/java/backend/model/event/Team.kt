package backend.model.event

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.challenges.Challenge
import backend.model.challenges.ChallengeStatus.PROOF_ACCEPTED
import backend.model.challenges.ChallengeStatus.WITH_PROOF
import backend.model.location.Location
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.payment.TeamEntryFeeInvoice
import backend.model.payment.billableAmount
import backend.model.sponsoring.Sponsoring
import backend.model.user.Participant
import backend.util.data.ChallengeDonateSums
import backend.util.parallelStream
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.persistence.CascadeType.REMOVE

@Entity
class Team : BasicEntity {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(creator: Participant, name: String, description: String, event: Event) : this() {
        this.event = event
        this.addMember(creator)
        this.name = name
        this.description = description
        this.profilePic = Media("image")
        this.invoice = TeamEntryFeeInvoice(this, Money.of(BigDecimal.valueOf(60), "EUR"))
    }

    var hasStarted: Boolean = false

    lateinit var name: String

    @ManyToOne
    lateinit var event: Event

    @Column(columnDefinition = "TEXT")
    lateinit var description: String

    @OneToMany(cascade = arrayOf(ALL), mappedBy = "team", orphanRemoval = true)
    private var invitations: MutableList<Invitation> = ArrayList()

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    lateinit var profilePic: Media

    @ManyToMany
    val members: MutableSet<Participant> = HashSet()

    @OneToMany(cascade = arrayOf(REMOVE), mappedBy = "team", orphanRemoval = true)
    val locations: MutableList<Location> = ArrayList()

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "team")
    var invoice: TeamEntryFeeInvoice? = null

    @OneToMany(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "team")
    var sponsoring: MutableList<Sponsoring> = ArrayList()

    @OneToMany(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "team")
    var challenges: MutableList<Challenge> = ArrayList()

    private fun addMember(participant: Participant) {

        if(participant.participatedAtEvent(event)) {
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
        return this.members.filter { it.account == participant.account }.isNotEmpty()
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

    /**
     * Returns the teams current distance in km
     */
    fun getCurrentDistance(): Double {
        return this.locations.maxBy { it.distance }?.distance ?: 0.0
    }

    fun raisedAmountFromChallenges(): ChallengeDonateSums {
        return this.challenges.parallelStream()
                .map { challenge ->
                    when (challenge.status) {
                        WITH_PROOF -> ChallengeDonateSums(challenge.amount.numberStripped, BigDecimal.ZERO)
                        PROOF_ACCEPTED -> ChallengeDonateSums(BigDecimal.ZERO, challenge.amount.numberStripped)
                        else -> ChallengeDonateSums(BigDecimal.ZERO, BigDecimal.ZERO)
                    }
                }
                .reduce { acc: ChallengeDonateSums, one: ChallengeDonateSums ->
                    ChallengeDonateSums(
                            acc.withProofSum + one.withProofSum,
                            acc.acceptedProofSum + one.acceptedProofSum)
                }
                .orElseGet {
                    ChallengeDonateSums(BigDecimal.ZERO, BigDecimal.ZERO)
                }
    }

    fun raisedAmountFromSponsorings(): Money {
        return this.sponsoring.billableAmount()
    }
}
