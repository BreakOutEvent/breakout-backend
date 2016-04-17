package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.EmailAddress
import org.codehaus.jackson.annotate.JsonIgnore
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.OneToOne

@Entity
class Invitation : BasicEntity {

    lateinit var status: InvitationStatus

    @OneToOne
    lateinit var team: Team

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
