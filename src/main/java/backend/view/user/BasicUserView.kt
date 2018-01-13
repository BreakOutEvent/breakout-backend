package backend.view.user

import backend.model.user.Participant
import backend.model.user.User
import backend.view.MediaView
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import javax.validation.Valid

class BasicUserView() {

    @JsonIgnore
    var user: User? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
    var firstname: String? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
    var lastname: String? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
    var gender: String? = null

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
        this.id = user.account.id
        this.gender = user.gender
        this.isBlocked = user.isBlocked
        this.participant = if (user.hasRole(Participant::class)) BasicParticipantViewModel(user) else null
        this.profilePic = user.profilePic?.let(::MediaView)
        this.roles = user.account.getAuthorities().map { it.authority }
    }

    class BasicParticipantViewModel() {

        @JsonIgnore
        var participant: Participant? = null
        var eventId: Long? = null
        var eventCity: String? = null
        var teamId: Long? = null
        var teamName: String? = null
        var tshirtSize: String? = null

        constructor(user: User) : this() {
            this.participant = user.getRole(Participant::class)
            this.eventId = participant?.getCurrentTeam()?.event?.id
            this.eventCity = participant?.getCurrentTeam()?.event?.city
            this.teamId = participant?.getCurrentTeam()?.id
            this.teamName = participant?.getCurrentTeam()?.name
            this.tshirtSize = participant?.tshirtsize
        }
    }
}
