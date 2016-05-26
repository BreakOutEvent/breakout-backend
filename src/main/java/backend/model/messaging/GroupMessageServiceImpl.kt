package backend.model.messaging

import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.user.UserCore
import backend.services.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupMessageServiceImpl @Autowired constructor(val repository: GroupMessageRepository, val mailService: MailService) : GroupMessageService {

    @Transactional
    override fun createGroupMessage(creator: UserCore): GroupMessage {
        val groupMessage = GroupMessage(creator)
        return repository.save(groupMessage)
    }

    override fun getByID(id: Long): GroupMessage? = repository.findById(id)

    override fun save(groupMessage: GroupMessage): GroupMessage = repository.save(groupMessage)

    @Transactional
    override fun addUser(user: UserCore, groupMessage: GroupMessage): GroupMessage {
        groupMessage.addUser(user)

        val email = Email(
                to = listOf(EmailAddress(user.email)),
                subject = "BreakOut 2016 - Euch wurde eine Challenge gestellt!",
                body = "Hallo ${user.firstname ?: ""},<br><br>" +
                        "Du wurdest einer neuen Gruppennachricht hinzugefügt. Sieh nach was man Dir mitteilen mag!<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "NACHRICHT ÖFFNEN",
                buttonUrl = "https://anmeldung.break-out.org/messages/${groupMessage.id}?utm_source=backend&utm_medium=email&utm_content=intial&utm_campaign=add_groupmessage",
                campaignCode = "add_groupmessage"
        )

        mailService.send(email)

        return repository.save(groupMessage)
    }

    @Transactional
    override fun addMessage(message: Message, groupMessage: GroupMessage): GroupMessage {
        groupMessage.addMessage(message)
        return repository.save(groupMessage)
    }
}
