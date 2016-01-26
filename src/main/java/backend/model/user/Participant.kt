@file:JvmName("Participant")

package backend.model.user

import backend.model.event.Team
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
@DiscriminatorValue("PARTICIPANT")
class Participant : UserRole {

    var emergencynumber: String = ""
    var tshirtsize: String? = null
    var hometown: String? = null
    var phonenumber: String? = null

    @ManyToOne
    var currentTeam: Team? = null

    constructor() : super()
    constructor(core: UserCore) : super(core)

    override fun getAuthority(): String = "PARTICIPANT"
}
