package backend.view

import backend.model.post.MediaSize

class MediaSizeView() {

    var id: Long? = null

    var url: String? = null

    var width: Int? = null

    var height: Int? = null

    var length: Float? = null

    constructor(mediaSize: MediaSize) : this() {
        this.id = mediaSize.id
        this.url = mediaSize.url
        this.width = mediaSize.width
        this.height = mediaSize.height
        this.length = mediaSize.length
    }
}
