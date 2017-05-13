package backend.services

import backend.model.messaging.Message
import backend.model.user.UserAccount

interface NotificationService {
    fun send(message: Message, users: List<UserAccount>)
}