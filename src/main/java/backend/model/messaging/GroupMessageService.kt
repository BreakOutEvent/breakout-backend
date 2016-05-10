package backend.model.messaging

import backend.model.user.UserCore

interface GroupMessageService {

    fun createGroupMessage(creator: UserCore): GroupMessage

    fun getByID(id: Long): GroupMessage?

    fun save(groupMessage: GroupMessage): GroupMessage

    fun addUser(user: UserCore, groupMessage: GroupMessage): GroupMessage

    fun addMessage(message: Message, groupMessage: GroupMessage): GroupMessage
}
