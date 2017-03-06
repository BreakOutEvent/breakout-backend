package backend.view

import backend.model.event.Team
import backend.model.location.Location
import java.util.*

class TeamLocationView {


    var id: Long? = null

    var name: String? = null

    var event: Long? = null

    var description: String? = null

    var hasStarted: Boolean? = null

    var members: MutableList<BasicUserView>? = null

    var profilePic: MediaView? = null

    var locations: List<BasicLocationView>? = null

    constructor(team: Team, locations: Iterable<Location>) {
        this.id = team.id
        this.name = team.name
        this.event = team.event.id
        this.description = team.description
        this.members = ArrayList()
        team.members.forEach { this.members!!.add(BasicUserView(it)) }
        this.profilePic = MediaView(team.profilePic)
        this.hasStarted = team.hasStarted
        this.locations = locations.map(::BasicLocationView)
    }


}
