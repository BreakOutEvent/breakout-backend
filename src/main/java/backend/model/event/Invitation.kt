package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.EmailAddress
import javax.persistence.Embedded
import javax.persistence.Entity

@Entity
class Invitation() : BasicEntity() {

    lateinit var status: InvitationStatus

    @Embedded
    lateinit var invitee: EmailAddress

    constructor(email: EmailAddress) : this() {
        this.status = InvitationStatus.OPEN
        this.invitee = email
    }

    enum class InvitationStatus {
        OPEN, ACCEPTED, REJECTED
    }
}
