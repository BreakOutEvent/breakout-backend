package backend.model.messaging

import backend.model.user.UserAccount

interface GroupMessageService {

    fun createGroupMessage(creator: UserAccount): GroupMessage

    fun getByID(id: Long): GroupMessage?

    fun save(groupMessage: GroupMessage): GroupMessage

    fun addUser(user: UserAccount, groupMessage: GroupMessage): GroupMessage

    fun addMessage(message: Message, groupMessage: GroupMessage): GroupMessage
}
