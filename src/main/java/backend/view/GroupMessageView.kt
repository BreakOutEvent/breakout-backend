package backend.view

import backend.model.messaging.GroupMessage
import backend.removeBlockedBy
import backend.view.user.BasicUserView
import java.util.*

class GroupMessageView() {

    var id: Long? = null

    var users: List<BasicUserView> = ArrayList()

    var messages: List<MessageView> = ArrayList()

    constructor(groupMessage: GroupMessage) : this() {
        this.id = groupMessage.id
        this.users = groupMessage.users.map(::BasicUserView)
        this.messages = groupMessage.messages.map(::MessageView)
    }
}
