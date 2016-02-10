package backend.model.post

import backend.model.BasicEntity
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class Media() : BasicEntity() {

    @ManyToOne
    var post: Post? = null

    var mediaType: MediaType? = null

    constructor(post: Post, type: String) : this() {
        this.mediaType = MediaType.valueOf(type.toUpperCase())
        this.post = post
    }

}