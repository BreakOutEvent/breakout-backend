package backend.model.user

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("ADMIN")
class Admin : UserRole {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(core: UserCore) : super(core)

    override fun getAuthority(): String = "ADMIN"
}
