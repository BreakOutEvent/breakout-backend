package backend.controller.ResponseBodies

import backend.model.user.Participant
import backend.model.user.User
import com.fasterxml.jackson.annotation.JsonIgnore

class UserViewModel() {

    @JsonIgnore
    var user: User? = null
    var firstname: String? = null
    var lastname: String? = null
    var email: String? = null
    var gender: String? = null
    var id: Long? = null
    var isBlocked: Boolean? = null
    var participant: ParticipantViewModel? = null

    constructor(user: User) : this() {
        this.user = user
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.email = user.email
        this.gender = user.gender
        this.id = user.core!!.id
        this.isBlocked = user.isBlocked
        this.participant = if (user.hasRole(Participant::class.java)) ParticipantViewModel(user) else null
    }
}

class ParticipantViewModel() {

    @JsonIgnore
    var participant: Participant? = null
    var emergencynumber: String ? = null
    var phonenumber: String? = null
    var hometown: String? = null
    var tshirtsize: String? = null

    constructor(user: User) : this() {
        this.participant = user.getRole(Participant::class.java) as Participant
        this.emergencynumber = participant!!.emergencynumber
        this.hometown = participant!!.hometown
        this.phonenumber = participant!!.phonenumber
        this.tshirtsize = participant!!.tshirtsize
    }
}
