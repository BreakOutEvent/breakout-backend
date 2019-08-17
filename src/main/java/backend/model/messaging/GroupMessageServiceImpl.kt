package backend.model.messaging

import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.UserAccount
import backend.model.user.UserRepository
import backend.services.NotificationService
import backend.services.mail.MailService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupMessageServiceImpl(val repository: GroupMessageRepository,
                              val userRepository: UserRepository,
                              val mailService: MailService,
                              val notificationServer: NotificationService) : GroupMessageService {

    @Transactional
    override fun createGroupMessage(creator: UserAccount): GroupMessage {
        val groupMessage = GroupMessage(creator)
        return repository.save(groupMessage)
    }

    override fun getByID(id: Long): GroupMessage? = repository.findById(id)

    override fun save(groupMessage: GroupMessage): GroupMessage = repository.save(groupMessage)

    @Transactional
    override fun addUser(user: UserAccount, groupMessage: GroupMessage): GroupMessage {
        groupMessage.addUser(user)

        val email = Email(
                to = listOf(EmailAddress(user.email)),
                subject = "BreakOut - Du wurdest einer Gruppennachricht hinzugefügt!",
                body = "Hallo ${user.firstname ?: ""},<br><br>" +
                        "Du wurdest einer neuen Gruppennachricht hinzugefügt. Sieh nach was man Dir mitteilen mag!<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "NACHRICHT ÖFFNEN",
                buttonUrl = "https://anmeldung.break-out.org/messages/${groupMessage.id}?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=add_groupmessage",
                campaignCode = "add_groupmessage"
        )

        mailService.send(email)
        notificationServer.notifyAddedToMessage(groupMessage.id, user)


        return repository.save(groupMessage)
    }

    @Transactional
    override fun addMessage(message: Message, groupMessage: GroupMessage): GroupMessage {
        userRepository.save(message.creator)
        groupMessage.addMessage(message)
        val notifiedUsers = groupMessage.users.filter { it.id != message.creator?.id && !groupMessage.isBlockedBy(it.id) }
        notificationServer.notifyNewMessage(message, groupMessage.id, notifiedUsers)
        return repository.save(groupMessage)
    }
}
