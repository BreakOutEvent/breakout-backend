package backend.services

import backend.model.misc.Email

interface MailService {
    fun send(email: Email)
}
