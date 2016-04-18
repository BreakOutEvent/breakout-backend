package backend.model.event

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.event.Invitation.InvitationStatus
import backend.model.location.Location
import backend.model.media.Media
import backend.model.misc.EmailAddress
import backend.model.user.Participant
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.ALL

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
    }

    lateinit var name: String

    @ManyToOne
    lateinit var event: Event

    lateinit var description: String

    @OneToOne(cascade = arrayOf(ALL))
    private var invitation: Invitation? = null

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    lateinit var profilePic: Media

    @OneToMany(mappedBy = "currentTeam", fetch = FetchType.EAGER)
    val members: MutableSet<Participant> = HashSet()

    @OneToMany(cascade = arrayOf(CascadeType.REMOVE), mappedBy = "team", orphanRemoval = true)
    val locations: MutableList<Location> = ArrayList()

    private fun addMember(participant: Participant) {
        if (participant.currentTeam != null) throw DomainException("Participant ${participant.email} already is part of a team")
        if (this.isFull()) throw DomainException("This team already has two members")

        members.add(participant)
        participant.currentTeam = this
    }

    @Throws
    fun join(participant: Participant) {

        if (invitation == null) {
            throw DomainException("${participant.email} can't join team $id because there are no invitations")
        } else if (invitation!!.invitee.toString() != participant.email) {
            throw DomainException("${participant.email} is not invited to join this team")
        }

        addMember(participant)
        invitation!!.status = InvitationStatus.ACCEPTED
    }

    @Throws
    fun invite(email: EmailAddress) {
        if (this.invitation != null) throw DomainException("Someone else has already been invited to this team")
        this.invitation = Invitation(email, this)
    }

    fun isMember(username: String): Boolean {
        return this.members.map { participant -> participant.email }.contains(username)
    }

    fun isMember(participant: Participant): Boolean {
        return this.members.filter { it.isSameUserAs(participant) }.isNotEmpty()
    }

    fun isFull(): Boolean {
        return this.members.count() >= 2
    }

    @PreRemove
    fun preRemove() {
        this.members.forEach { it.currentTeam = null }
        this.members.clear()
        this.locations.forEach { it.team = null }
        this.locations.clear()
    }
}
