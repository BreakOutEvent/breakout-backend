package backend.model.messaging

import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupMessageServiceImpl @Autowired constructor(val repository: GroupMessageRepository) : GroupMessageService {

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

        return repository.save(groupMessage)
    }

    @Transactional
    override fun addMessage(message: Message, groupMessage: GroupMessage): GroupMessage {
        groupMessage.addMessage(message)
        return repository.save(groupMessage)
    }
}
