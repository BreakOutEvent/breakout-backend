package backend.services.fakeservices

import backend.model.messaging.Message
import backend.model.user.UserAccount
import backend.services.NotificationService
import backend.util.Profiles.TEST
import backend.util.Profiles.DEVELOPMENT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(DEVELOPMENT, TEST)
class FakeNotificationService: NotificationService {

    val logger: Logger = LoggerFactory.getLogger(FakeNotificationService::class.java)

    override fun send(message: Message, groupId: Long?, users: List<UserAccount>) {
        logger.info("""Fakely sending "$message.text" in group $groupId""")
    }

}