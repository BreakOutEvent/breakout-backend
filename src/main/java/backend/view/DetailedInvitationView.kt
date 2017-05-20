package backend.view

import backend.model.event.Invitation

class DetailedInvitationView(invitation: Invitation) {

    val teamId: Long = invitation.team!!.id!!
    val teamName: String? = invitation.team?.name
    val eventId: Long = invitation.team!!.event.id!!
    val eventCity: String = invitation.team!!.event.city
    val creator: String = invitation.team!!.members.first().email
    val email: String = invitation.invitee.toString()
    val token: String = invitation.invitationToken

}