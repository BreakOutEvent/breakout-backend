package backend.model.post

import backend.model.BasicEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.ManyToOne

@Entity
class MediaSize() : BasicEntity() {

    @ManyToOne
    var media: Media? = null

    var url: String? = null

    var width: Int? = null

    var height: Int? = null

    var length: Int? = null

    var size: Long? = null

    @Enumerated(EnumType.STRING)
    var mediaType: MediaType? = null

    constructor(media: Media, url: String, width: Int, height: Int, length: Int, size: Long, type: String) : this() {
        this.media = media
        this.url = url
        this.width = width
        this.height = height
        this.length = length
        this.size = size
        this.mediaType = MediaType.valueOf(type.toUpperCase())
    }

}