package backend.model.posting

import backend.model.BasicEntity
import backend.model.user.UserCore
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.PreRemove

@Entity
class Comment : BasicEntity {

    private constructor() : super()

    @Column(columnDefinition = "TEXT")
    lateinit var text: String

    lateinit var date: LocalDateTime

    @ManyToOne
    lateinit var posting: Posting

    @ManyToOne
    var user: UserCore? = null

    constructor(text: String, date: LocalDateTime, posting: Posting, user: UserCore) : this() {
        this.text = text
        this.date = date
        this.posting = posting
        this.user = user
    }


    @PreRemove
    fun preRemove() {
        this.posting.comments.remove(this)
        this.user = null
    }
}
