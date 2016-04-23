package backend.view

import backend.model.user.Participant
import backend.model.user.User
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.validation.Valid

class BasicUserView() {

    @JsonIgnore
    var user: User? = null

    var firstname: String? = null
    var lastname: String? = null

    var id: Long? = null
    var isBlocked: Boolean? = null

    @Valid
    var participant: BasicParticipantViewModel? = null

    var profilePic: MediaView? = null

    var roles: List<String> = arrayListOf()

    constructor(user: User) : this() {
        this.user = user
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.id = user.core.id
        this.isBlocked = user.isBlocked
        this.participant = if (user.hasRole(Participant::class)) BasicParticipantViewModel(user) else null
        this.profilePic = MediaView(user.profilePic)
        this.roles = user.core.getAuthorities().map { it.authority }
    }

    class BasicParticipantViewModel() {

        @JsonIgnore
        var participant: Participant? = null
        var eventId: Long? = null
        var eventCity: String? = null
        var teamId: Long? = null
        var teamName: String? = null

        constructor(user: User) : this() {
            this.participant = user.getRole(Participant::class)
            this.eventId = participant?.currentTeam?.event?.id
            this.eventCity = participant?.currentTeam?.event?.city
            this.teamId = participant?.currentTeam?.id
            this.teamName = participant?.currentTeam?.name
        }
    }
}
