package backend.model.posting

import backend.model.BasicEntity
import backend.model.user.UserAccount
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.PreRemove
import javax.persistence.Table

@Entity

@Table(name = "postinglike")
class Like : BasicEntity {

    private constructor() : super()

    lateinit var date: LocalDateTime

    @ManyToOne
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
}
