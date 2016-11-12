package backend.util

import backend.model.user.Participant

fun getBankingSubject(participant: Participant): String {
    return getBankingSubject(participant.currentTeam!!.id!!, participant.firstname ?: "", participant.lastname ?: "")
}

fun getBankingSubject(teamId: Long, firstname: String, lastname: String): String {
    var subject: String = "$teamId-BO16-$firstname-$lastname"
    subject = subject
            .replace("Ä", "Ae").replace("Ü", "Ue").replace("Ö", "Oe")
            .replace("ä", "ae").replace("ü", "ue").replace("ö", "oe").replace("ß", "ss")
            .replace(Regex("[^A-Za-z0-9-]"), "")
    if (subject.length > 140) {
        return subject.substring(0, 140)
    } else {
        return subject
    }
}