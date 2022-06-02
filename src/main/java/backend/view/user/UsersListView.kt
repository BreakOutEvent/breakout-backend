package backend.view.user


import backend.model.user.Participant
import backend.model.user.User
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.SafeHtml
import javax.validation.Valid

class UsersListView() {
    @JsonIgnore
    var user: User? = null

    @Valid
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    var firstname: String? = null

    @Valid
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    var lastname: String? = null

    @Valid
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    var email: String? = null

    @Valid
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    var gender: String? = null

    var id: Long? = null

    @Valid
    var participant: ParticipantViewModel? = null

    constructor(user: User) : this() {
        this.user = user
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.email = user.email
        this.id = user.account.id
        this.gender = user.gender
        this.participant = if (user.hasRole(Participant::class)) ParticipantViewModel(user) else null
    }
    class ParticipantViewModel() {

        @JsonIgnore
        var participant: Participant? = null
        var eventId: Long? = null
        var eventCity: String? = null
        var teamId: Long? = null
        var teamName: String? = null
        var tshirtSize: String? = null
        var address: String? = null

        constructor(user: User) : this() {
            this.participant = user.getRole(Participant::class)
            this.eventId = participant?.getCurrentTeam()?.event?.id
            this.eventCity = participant?.getCurrentTeam()?.event?.city
            this.teamId = participant?.getCurrentTeam()?.id
            this.teamName = participant?.getCurrentTeam()?.name
            this.tshirtSize = participant?.tshirtsize
            this.address = participant?.getCurrentTeam()?.postaddress
        }
    }
}

