package backend.model.messaging

import backend.model.BasicEntity
import backend.model.user.UserAccount
import java.util.*
import javax.persistence.*

@Entity
class GroupMessage : BasicEntity {

    private constructor() : super()

    @ManyToMany
    var users: MutableList<UserAccount> = ArrayList()

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    @OrderBy("date ASC")
    var messages: MutableList<Message> = ArrayList()

    constructor(creator: UserAccount) : this() {
        this.users.add(creator)
    }

    fun addUser(user: UserAccount) {
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
