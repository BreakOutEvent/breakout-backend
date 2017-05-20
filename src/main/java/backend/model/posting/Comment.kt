package backend.model.posting

import backend.model.BasicEntity
import backend.model.user.UserAccount
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Comment : BasicEntity {

    private constructor() : super()

    @Column(columnDefinition = "TEXT")
    lateinit var text: String

    lateinit var date: LocalDateTime

    @ManyToOne(fetch = FetchType.LAZY)
    var user: UserAccount? = null

    constructor(text: String, date: LocalDateTime, user: UserAccount) : this() {
        this.text = text
        this.date = date
        this.user = user
    }

    @PreRemove
    fun preRemove() {
        this.user = null
    }
}
