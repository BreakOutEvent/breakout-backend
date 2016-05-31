package backend.view

import backend.model.user.Participant
import backend.model.user.User

class SimpleUserView() {

    var id: Long? = null

    var firstname: String? = null

    var lastname: String? = null

    var teamId: Long? = null

    var teamname: String? = null

    constructor(user: User) : this() {
        this.id = user.core.id
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.teamId = user.getRole(Participant::class)?.currentTeam?.id
        this.teamname = user.getRole(Participant::class)?.currentTeam?.name
    }
}
