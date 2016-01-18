package backend.model.event

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Invitation {

    @Id
    @GeneratedValue
    var id: Long? = null

    var status: InvitationStatus
    var invitee: String

    constructor(email: String) {
        this.status = InvitationStatus.OPEN
        this.invitee = email
    }

    enum class InvitationStatus {
        OPEN, ACCEPTED, REJECTED
    }
}
