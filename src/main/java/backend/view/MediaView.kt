package backend.view

import backend.model.post.Media
class MediaView() {

    var id: Long? = null

    var type: String? = null

    constructor(media: Media) : this() {
        this.id = media.id
        this.type = media.mediaType.toString()
    }
}
