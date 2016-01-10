package backend.controller.ViewModels

import backend.model.user.Participant
import backend.model.user.User
import com.fasterxml.jackson.annotation.JsonIgnore

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

