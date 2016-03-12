package backend.view

import backend.model.event.Team
import org.hibernate.validator.constraints.NotEmpty
import java.util.*
import javax.validation.constraints.NotNull

class TeamRequestView() {

    var id: Long? = null

    @NotEmpty
    var name: String? = null

    @NotNull
    var event: Long? = null

    @NotNull
    var description: String? = null

    var members: MutableList<String>? = null

    var profilePic: String? = null

    constructor(team: Team) : this() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description
        this.members = ArrayList<String>()
        team.members.forEach { this.members!!.add(it.email) }
        this.profilePic = team.profilePic.mediaType.toString()
    }
}
