package backend.model.user

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("EVENT_MANAGER")
class EventManager : UserRole {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(account: UserAccount) : super(account)

    override fun getAuthority(): String = "EVENT_MANAGER"
}
