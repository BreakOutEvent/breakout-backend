package backend.model.sponsoring

import backend.model.challenges.Challenge
import backend.model.misc.Url
import backend.model.user.Address
import backend.model.user.Sponsor
import backend.model.media.Media

interface ISponsor {
    // TODO: Was braucht ein Sponsor??
    // TODO: Was ist nullable?
    var firstname: String?

    var lastname: String?

    var company: String?

    var address: Address?

    var isHidden: Boolean

    var url: Url?

    var logo: Media?

    var supporterType: SupporterType

    @Deprecated("Just a workaround")
    var registeredSponsor: Sponsor? // TODO: Fix this. Just a workaround!

    @Deprecated("Just a workaround")
    var unregisteredSponsor: UnregisteredSponsor? // TODO: Fix this. Just a workaround!

    var challenges: MutableList<Challenge>

    var sponsorings: MutableList<Sponsoring>
}
