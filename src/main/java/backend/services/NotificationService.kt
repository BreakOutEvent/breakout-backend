package backend.services

import backend.model.messaging.Message
import backend.model.user.UserAccount

interface NotificationService {
    fun notifyNewMessage(message: Message, groupId: Long?, users: List<UserAccount>)
}