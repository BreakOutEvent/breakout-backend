package backend.view

import backend.model.event.WhitelistDomainEntry

class WhitelistDomainView() {

    var eventId: Long? = null

    var domain: String = ""

    constructor(whitelistDomainEntry: WhitelistDomainEntry) : this() {
        this.eventId = whitelistDomainEntry.event.id
        this.domain = whitelistDomainEntry.domain
    }
}