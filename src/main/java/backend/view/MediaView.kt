package backend.view

import backend.model.media.Media

class MediaView() {

    var id: Long? = null

    var type: String? = null

    var uploadToken: String? = null

    var sizes: List<MediaSizeView>? = null

    constructor(media: Media) : this() {
        this.id = media.id
        this.type = media.mediaType.toString()
        this.uploadToken = media.uploadToken
        this.sizes = media.sizes?.map { MediaSizeView(it) }
    }
}
