package backend.view

import backend.model.media.MediaSize

class MediaSizeView() {

    var id: Long? = null

    var url: String? = null

    var width: Int? = null

    var height: Int? = null

    var length: Int? = null

    var size: Long? = null

    var type: String? = null

    constructor(mediaSize: MediaSize) : this() {
        this.id = mediaSize.id
        this.url = mediaSize.url
        this.width = mediaSize.width
        this.height = mediaSize.height
        this.length = mediaSize.length
        this.size = mediaSize.size
        this.type = mediaSize.mediaType.toString()
    }
}
