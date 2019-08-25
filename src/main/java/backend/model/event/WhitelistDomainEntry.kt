package backend.model.event

import backend.model.BasicEntity
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne

@Entity
class WhitelistDomainEntry : BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var event: Event

    lateinit var domain: String

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(domain: String, event: Event) : this() {
        this.domain = domain
        this.event = event
    }
}
