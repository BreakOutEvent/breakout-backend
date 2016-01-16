package backend.model.event

import backend.model.misc.Coords
import backend.model.user.User
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Post() {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    var id: Long? = null

    lateinit var text: String
    lateinit var date: LocalDateTime

    @Embedded
    lateinit var postLocation: Coords

    @Embedded
    lateinit var user: User

    constructor(text: String, postLocation: Coords, user: User) : this() {
        this.text = text
        this.date = LocalDateTime.now()
        this.postLocation = postLocation
        this.user = user
    }

}
