package backend.services.mail

import backend.model.misc.Email

interface MailSenderService {
    fun send(email: Email, saveToDb: Boolean = false)

    fun resendFailed(): Int

    fun sendAsync(email: Email, saveToDb: Boolean = false)
}

