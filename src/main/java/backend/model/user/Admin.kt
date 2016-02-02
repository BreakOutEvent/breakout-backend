package backend.model.user

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("ADMIN")
class Admin : UserRole {
    constructor() : super()
    constructor(core: UserCore) : super(core)
    override fun getAuthority(): String? = "ROLE_ADMIN"
}
