package backend.view

import backend.model.messaging.GroupMessage
import java.util.*

class GroupMessageView() {

    var id: Long? = null

    var users: List<BasicUserView> = ArrayList()

    var messages: List<MessageView> = ArrayList()

    constructor(groupMessage: GroupMessage) : this() {
        this.id = groupMessage.id
        this.users = groupMessage.users.map { BasicUserView(it) }
        this.messages = groupMessage.messages.map { MessageView(it) }
    }
}
