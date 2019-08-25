package backend.view

import backend.model.event.WhitelistEmailEntry

class WhitelistEmailView() {

    var eventId: Long? = null

    var email: String = ""

    constructor(whitelistEmailEntry: WhitelistEmailEntry) : this() {
        this.eventId = whitelistEmailEntry.event.id
        this.email = whitelistEmailEntry.invitee.value
    }
}