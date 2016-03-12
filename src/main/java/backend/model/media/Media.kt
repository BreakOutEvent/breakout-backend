package backend.model.media

import backend.model.BasicEntity
import backend.model.posting.Posting
import java.util.*
import javax.persistence.*

@Entity
class Media() : BasicEntity() {


    @Enumerated(EnumType.STRING)
    var mediaType: MediaType? = null

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var sizes: MutableList<MediaSize>? = ArrayList()

    @Transient
    var uploadToken: String? = null

    constructor(type: String) : this() {
        this.mediaType = MediaType.valueOf(type.toUpperCase())
    }

}