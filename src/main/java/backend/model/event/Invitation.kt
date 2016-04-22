package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.EmailAddress
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class Invitation : BasicEntity {

    lateinit var status: InvitationStatus

    @ManyToOne
    var team: Team? = null

    @Embedded
    lateinit var invitee: EmailAddress

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(email: EmailAddress, team: Team) : this() {
        this.status = InvitationStatus.OPEN
        this.invitee = email
        this.team = team
    }

    enum class InvitationStatus {
        OPEN, ACCEPTED, REJECTED
    }
}
