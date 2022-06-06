package backend.view.user

import backend.model.user.Participant
import backend.model.user.User
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

class ParticipantViewModel {
    var id: Long? = null
    var eventId: Long? = null
    var teamId: Long? = null
    var teamName: String? = null
    var gender: String? = null
    var firstname: String? = null
    var lastname: String? = null
    var emergencynumber: String? = null
    var tshirtsize: String? = null
    var hometown: String? = null
    var phonenumber: String? = null
    var birthdate: String? = null

    constructor(participant: Participant) {
        this.id = participant?.id
        this.eventId = participant?.getCurrentTeam()?.event?.id
        this.teamId = participant?.getCurrentTeam()?.id
        this.teamName = participant?.getCurrentTeam()?.name
        this.gender = participant?.gender
        this.firstname = participant?.account?.firstname
        this.lastname = participant?.account?.lastname
        this.emergencynumber = participant?.emergencynumber
        this.tshirtsize = participant?.tshirtsize
        this.hometown = participant?.hometown
        this.phonenumber = participant?.phonenumber
        this.birthdate = participant?.birthdate?.toString()
    }
}