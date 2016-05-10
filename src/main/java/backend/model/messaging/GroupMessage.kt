package backend.model.messaging

import backend.model.BasicEntity
import backend.model.user.UserCore
import java.util.*
import javax.persistence.*

@Entity
class GroupMessage : BasicEntity {

    private constructor() : super()

    @ManyToMany
    var users: MutableList<UserCore> = ArrayList()

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var messages: MutableList<Message> = ArrayList()

    constructor(creator: UserCore) : this() {
        this.users.add(creator)
    }

    fun addUser(user: UserCore) {
        this.users.add(user)
    }

    fun addMessage(message: Message) {
        this.messages.add(message)
    }

    @PreRemove
    fun preRemove() {
        this.users.clear()
        this.messages.clear()
    }
}
