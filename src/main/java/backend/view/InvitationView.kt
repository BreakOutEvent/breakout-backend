package backend.view

import backend.model.event.Invitation

class InvitationView() {
    var team: Long? = null
    var name: String? = null

    constructor(invitation: Invitation) : this() {
        this.team = invitation.team?.id
        this.name = invitation.team?.name
    }
}
