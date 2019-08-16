package backend.view.user

import backend.model.user.Admin
import backend.model.user.Participant
import backend.model.user.User

open class SimpleUserView() {

    var id: Long? = null

    var firstname: String? = null

    var lastname: String? = null

    var teamId: Long? = null

    var teamname: String? = null

    var isAdmin: Boolean? = null

    constructor(user: User) : this() {
        this.id = user.account.id
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.teamId = user.getRole(Participant::class)?.getCurrentTeam()?.id
        this.teamname = user.getRole(Participant::class)?.getCurrentTeam()?.name
        this.isAdmin = user.hasRole(Admin::class)
    }
}

class AdminSimpleUserView: SimpleUserView {
    var email: String? = null

    constructor(user: User) : super(user) {
        email = user.email
    }
}
