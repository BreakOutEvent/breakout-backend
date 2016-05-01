package backend.view

import backend.model.event.Invitation

class DetailedInvitationView {

    val teamId: Long
    val teamName: String?
    val eventId: Long
    val eventCity: String
    val creator: String
    val email: String
    val token: String

    constructor(invitation: Invitation) {
        this.teamId = invitation.team!!.id!!
        this.teamName = invitation.team?.name
        this.eventId = invitation.team!!.event.id!!
        this.eventCity = invitation.team!!.event.city
        this.creator = invitation.team!!.members.first().email
        this.email = invitation.invitee.toString()
        this.token = invitation.invitationToken
    }
}