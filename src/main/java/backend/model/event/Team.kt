package backend.model.event

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.location.Location
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.payment.TeamEntryFeeInvoice
import backend.model.user.Participant
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.*

@Entity
class Team : BasicEntity {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(creator: Participant, name: String, description: String, event: Event) : this() {
        this.addMember(creator)
        this.name = name
        this.description = description
        this.event = event
        this.profilePic = Media("image")
        this.invoice = TeamEntryFeeInvoice(this, Money.of(BigDecimal.valueOf(60), "EUR"))
    }

    lateinit var name: String

    @ManyToOne
    lateinit var event: Event

    lateinit var description: String

    @OneToMany(cascade = arrayOf(ALL), mappedBy = "team", orphanRemoval = true)
    private var invitations: MutableList<Invitation> = ArrayList()

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    lateinit var profilePic: Media

    @OneToMany(mappedBy = "currentTeam", fetch = FetchType.EAGER)
    val members: MutableSet<Participant> = HashSet()

    @OneToMany(cascade = arrayOf(REMOVE), mappedBy = "team", orphanRemoval = true)
    val locations: MutableList<Location> = ArrayList()

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "team")
    var invoice: TeamEntryFeeInvoice? = null

    private fun addMember(participant: Participant) {
        if (participant.currentTeam != null) throw DomainException("Participant ${participant.email} already is part of a team")
        if (this.isFull()) throw DomainException("This team already has two members")

        members.add(participant)
        participant.currentTeam = this
    }

    @Throws
    fun join(participant: Participant) {

        val inviteeEmail = EmailAddress(participant.email)
        if (!isInvited(inviteeEmail)) {
            throw DomainException("${participant.email} can't join team because he is not invited")
        } else if (isFull()) {
            throw DomainException("${participant.email} can't join team because this team is already full")
        } else {
            addMember(participant)
        }
    }

    @Throws
    fun invite(email: EmailAddress): Invitation {
        if (isInvited(email)) throw DomainException("User ${email.toString()} already is invited to this team")
        val invitation = Invitation(email, this)
        this.invitations.add(invitation)
        return invitation
    }

    fun isInvited(email: EmailAddress): Boolean {
        return this.invitations.map { it.invitee }.contains(email)
    }

    //TODO: Suppress warning as this is used by an @PreAuthorize statement
    fun isMember(username: String): Boolean {
        return this.members.map { participant -> participant.email }.contains(username)
    }

    fun isMember(participant: Participant): Boolean {
        return this.members.filter { it.isSameUserAs(participant) }.isNotEmpty()
    }

    fun isFull(): Boolean {
        return this.members.count() >= 2
    }

    fun leave(participant: Participant) {
        if (!this.isMember(participant)) throw DomainException("Can't leave team because user never was a part of it")
        if (this.isFull()) throw DomainException("Can't leave team because it is already full")
        this.invitations.forEach { it.team = null }
        this.invitations.clear()

        this.members.forEach { it.currentTeam = null }
        this.members.clear()
    }

    @PreRemove
    fun preRemove() {
        this.members.forEach { it.currentTeam = null }
        this.members.clear()
        this.locations.forEach { it.team = null }
        this.locations.clear()
        this.invoice?.team = null
        this.invoice = null
        this.invitations.forEach { it.team = null }
        this.invitations.clear()
    }
}
