package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.EmailAddress
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne

@Entity
class WhitelistEmailEntry : BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var event: Event

    @Embedded
    lateinit var invitee: EmailAddress

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(email: EmailAddress, event: Event) : this() {
        this.invitee = email
        this.event = event
    }
}
