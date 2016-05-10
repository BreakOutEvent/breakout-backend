package backend.model.posting

import backend.model.BasicEntity
import backend.model.user.UserCore
import java.time.LocalDateTime
import javax.persistence.*

@Entity

@Table(
        name = "postinglike",
        uniqueConstraints = arrayOf(
                UniqueConstraint(columnNames = arrayOf("posting_id", "user_id"))
        )
)
class Like : BasicEntity {

    private constructor() : super()

    lateinit var date: LocalDateTime

    @ManyToOne
    lateinit var posting: Posting

    @ManyToOne
    var user: UserCore? = null

    constructor(date: LocalDateTime, posting: Posting, user: UserCore) : this() {
        this.date = date
        this.posting = posting
        this.user = user
    }

    @PreRemove
    fun preRemove() {
        this.posting.likes.remove(this)
        this.user = null
    }
}
