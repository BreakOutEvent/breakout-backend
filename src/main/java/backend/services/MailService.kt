package backend.services

import backend.model.misc.Email

interface MailService {
    fun send(email: Email, saveToDb: Boolean = false)
    fun resendFailed(): Int
    fun sendAsync(email: Email, saveToDb: Boolean = false)
}
