package backend.view

import backend.model.event.Team
import org.hibernate.validator.constraints.NotEmpty
import java.util.*

class TeamView() {

    var id: Long? = null

    @NotEmpty
    var name: String? = null

    var event: Long? = null

    var description: String = ""

    var members: MutableList<String>? = null

    var profilePic: MediaView? = null

    constructor(team: Team) : this() {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description
        this.members = ArrayList<String>()
        team.members.forEach { this.members!!.add(it.email) }
        this.profilePic = MediaView(team.profilePic)
    }
}
