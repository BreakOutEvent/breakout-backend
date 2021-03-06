package backend.view.user

import backend.model.user.Participant
import backend.model.user.User

open class SimpleUserView() {

    var id: Long? = null

    var firstname: String? = null

    var lastname: String? = null

    var teamId: Long? = null

    var teamname: String? = null

    constructor(user: User) : this() {
        this.id = user.account.id
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.teamId = user.getRole(Participant::class)?.getCurrentTeam()?.id
        this.teamname = user.getRole(Participant::class)?.getCurrentTeam()?.name
    }
}

class AdminSimpleUserView: SimpleUserView {
    var email: String? = null
    var roles: List<String> = arrayListOf()

    constructor(user: User) : super(user) {
        email = user.email
        roles = user.account.getAuthorities().map { it.authority }
    }
}
