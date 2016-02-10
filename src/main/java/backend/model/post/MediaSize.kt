package backend.model.post

import backend.model.BasicEntity
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class MediaSize() : BasicEntity() {

    @ManyToOne
    var media: Media? = null

    var url: String? = null

    var width: Int? = null

    var height: Int? = null

    var length: Float? = null

    constructor(media: Media, url: String, width: Int, height: Int, length: Float) : this() {
        this.media = media
        this.url = url
        this.width = width
        this.height = height
        this.length = length
    }

}