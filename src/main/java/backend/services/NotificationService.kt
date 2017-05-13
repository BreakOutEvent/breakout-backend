package backend.services

import backend.model.messaging.GroupMessage
import backend.model.user.UserAccount

interface NotificationService {
    fun send(title: String, subtitle: String?, data: GroupMessage, users: List<UserAccount>)
}