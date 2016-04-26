package backend.view

import backend.model.user.Participant
import backend.model.user.User
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.Email
import javax.validation.Valid
import javax.validation.constraints.NotNull

class UserView() {

    @JsonIgnore
    var user: User? = null

    var password: String? = null

    var firstname: String? = null
    var lastname: String? = null

    @Email
    var email: String? = null

    var gender: String? = null
    var id: Long? = null
    var isBlocked: Boolean? = null

    @Valid
    var participant: ParticipantViewModel? = null

    var profilePic: MediaView? = null

    var roles: List<String> = arrayListOf()

    constructor(user: User) : this() {
        this.user = user
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.email = user.email
        this.gender = user.gender
        this.id = user.core.id
        this.isBlocked = user.isBlocked
        this.participant = if (user.hasRole(Participant::class)) ParticipantViewModel(user) else null
        this.profilePic = MediaView(user.profilePic)
        this.roles = user.core.getAuthorities().map { it.authority }
    }

    class ParticipantViewModel() {

        @JsonIgnore
        var participant: Participant? = null

        @NotNull
        var emergencynumber: String ? = null

        @NotNull
        var phonenumber: String? = null

        @NotNull
        var tshirtsize: String? = null

        var birthdate: String? = null
        var hometown: String? = null
        var eventId: Long? = null
        var eventCity: String? = null
        var teamId: Long? = null
        var teamName: String? = null

        constructor(user: User) : this() {
            this.participant = user.getRole(Participant::class)
            this.emergencynumber = participant?.emergencynumber
            this.hometown = participant?.hometown
            this.birthdate = participant?.birthdate.toString()
            this.phonenumber = participant?.phonenumber
            this.tshirtsize = participant?.tshirtsize
            this.eventId = participant?.currentTeam?.event?.id
            this.eventCity = participant?.currentTeam?.event?.city
            this.teamId = participant?.currentTeam?.id
            this.teamName = participant?.currentTeam?.name
        }
    }
}
