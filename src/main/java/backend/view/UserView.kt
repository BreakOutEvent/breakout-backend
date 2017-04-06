package backend.view

import backend.model.user.*
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

    var preferredLanguage: String? = null

    @Email
    var email: String? = null

    var gender: String? = null
    var id: Long? = null
    var isBlocked: Boolean? = null

    @Valid
    var participant: ParticipantViewModel? = null

    @Valid
    var sponsor: SponsorView? = null

    var profilePic: MediaView? = null

    var roles: List<String> = arrayListOf()

    var groupMessageIds: List<Long> = arrayListOf()

    constructor(user: User) : this() {
        this.user = user
        this.firstname = user.firstname
        this.lastname = user.lastname
        this.email = user.email
        this.gender = user.gender
        this.id = user.account.id
        this.isBlocked = user.isBlocked
        this.participant = if (user.hasRole(Participant::class)) ParticipantViewModel(user) else null
        this.sponsor = if (user.hasRole(Sponsor::class)) SponsorView(user) else null
        this.profilePic = MediaView(user.profilePic)
        this.roles = user.account.getAuthorities().map { it.authority }
        this.groupMessageIds = user.account.groupMessages.map { it.id!! }
        this.preferredLanguage = when (user.preferredLanguage) {
            Language.EN -> "en"
            Language.DE -> "de"
        }
    }

    class ParticipantViewModel() {

        @JsonIgnore
        var participant: Participant? = null

        @NotNull
        var emergencynumber: String? = null

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
            this.eventId = participant?.getCurrentTeam()?.event?.id
            this.eventCity = participant?.getCurrentTeam()?.event?.city
            this.teamId = participant?.getCurrentTeam()?.id
            this.teamName = participant?.getCurrentTeam()?.name
        }
    }

    class SponsorView() {

        @JsonIgnore
        var sponsor: Sponsor? = null

        var company: String? = null

        var url: String? = null

        @Valid
        var address: AddressView? = null

        var isHidden: Boolean? = null

        constructor(user: User) : this() {
            this.sponsor = user.getRole(Sponsor::class)
            this.company = sponsor?.company
            this.url = sponsor?.url.toString()
            this.address = AddressView(sponsor?.address)
            this.isHidden = sponsor?.isHidden
        }
    }

    class AddressView() {

        @NotNull
        var street: String? = null

        @NotNull
        var housenumber: String? = null

        @NotNull
        var city: String? = null

        @NotNull
        var country: String? = null

        @NotNull
        var zipcode: String? = null

        constructor(address: Address?) : this() {
            this.street = address?.street
            this.housenumber = address?.housenumber
            this.city = address?.city
            this.country = address?.country
            this.zipcode = address?.zipcode
        }
    }
}
