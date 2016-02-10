package backend.model.post

import backend.model.BasicEntity
import java.util.*
import javax.persistence.*

@Entity
class Media() : BasicEntity() {

    @ManyToOne
    var post: Post? = null

    @Enumerated(EnumType.STRING)
    var mediaType: MediaType? = null

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var sizes: MutableList<MediaSize>? = ArrayList()

    constructor(post: Post, type: String) : this() {
        this.mediaType = MediaType.valueOf(type.toUpperCase())
        this.post = post
    }

}