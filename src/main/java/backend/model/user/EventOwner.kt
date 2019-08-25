package backend.model.user

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("EVENT_OWNER")
class EventOwner : UserRole {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(account: UserAccount) : super(account)

    override fun getAuthority(): String = "EVENT_OWNER"
    override fun getSubRoles(): Iterable<UserRole> = listOf(EventManager(account))
}