package backend.model.posting

import backend.model.BasicEntity
import backend.model.location.Location
import backend.model.media.Media
import backend.model.user.UserCore
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.PERSIST

@Entity
class Posting : BasicEntity {

    private constructor() : super()

    var text: String? = null

    @ElementCollection
    lateinit var hashtags: List<Hashtag>

    lateinit var date: LocalDateTime

    @OneToOne(cascade = arrayOf(PERSIST))
    var location: Location? = null

    @ManyToOne
    var user: UserCore? = null

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var media: MutableList<Media>? = ArrayList()

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var comments: MutableList<Comment> = ArrayList()

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var likes: MutableList<Like> = ArrayList()

    constructor(text: String?, date: LocalDateTime, location: Location?, user: UserCore, media: MutableList<Media>?) : this() {
        this.text = text
        this.date = date
        this.location = location
        this.user = user
        this.media = media
    }


    @PreRemove
    fun preRemove() {
        this.likes.clear()
        this.comments.clear()
        this.media?.clear()
        this.user = null
    }
}
