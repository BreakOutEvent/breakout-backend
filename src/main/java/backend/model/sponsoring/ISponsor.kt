package backend.model.sponsoring

import backend.model.user.Address
import backend.model.user.Sponsor

interface ISponsor {
    // TODO: Was braucht ein Sponsor??
    // TODO: Was ist nullable?
    var firstname: String?

    var lastname: String?

    var company: String?

    var address: Address

    var isHidden: Boolean

    @Deprecated("Just a workaround")
    var registeredSponsor: Sponsor? // TODO: Fix this. Just a workaround!

    @Deprecated("Just a workaround")
    var unregisteredSponsor: UnregisteredSponsor? // TODO: Fix this. Just a workaround!
}
