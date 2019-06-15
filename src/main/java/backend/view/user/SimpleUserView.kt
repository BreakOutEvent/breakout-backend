package backend.view.user

import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User

class SimpleUserView() {

    var id: Long? = null

    var firstname: String? = null

    var lastname: String? = null

    var teamId: Long? = null

    var teamname: String? = null

    var email: String? = null

    var isAdmin: Boolean? = null

    constructor(user: User) : this() {
        this.id = user.account.id
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.teamId = user.getRole(Participant::class)?.getCurrentTeam()?.id
        this.teamname = user.getRole(Participant::class)?.getCurrentTeam()?.name
        this.email = user.email
        this.isAdmin = user.hasRole(Admin::class)
    }
}
