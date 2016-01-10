@file:JvmName("Participant")

package backend.model.user

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("PARTICIPANT")
class Participant : UserRole {

    var emergencynumber: String = ""
    var tshirtsize: String? = null
    var hometown: String? = null
    var phonenumber: String? = null

    constructor() : super()
    constructor(core: UserCore) : super(core)

    override fun getAuthority(): String = "PARTICIPANT"
}
