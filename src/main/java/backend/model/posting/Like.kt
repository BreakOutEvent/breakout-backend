package backend.model.posting

import backend.model.UserGenerated
import backend.model.BasicEntity
import backend.model.user.User
import backend.model.user.UserAccount
import java.time.LocalDateTime
import javax.persistence.*

@Entity

@Table(name = "postinglike")
class Like : BasicEntity, UserGenerated {

    private constructor() : super()

    lateinit var date: LocalDateTime

    @ManyToOne(fetch = FetchType.LAZY)
    var user: UserAccount? = null

    constructor(date: LocalDateTime, user: UserAccount) : this() {
        this.date = date
        this.user = user
    }

    @PreRemove
    fun preRemove() {
        this.user = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Like) return false

        if (date != other.date) return false
        if (user!!.id == other.user!!.id) return false // TODO: Implement equals in UserAccount!

        return true
    }

    override fun getUser(): User? {
        return user
    }
}
