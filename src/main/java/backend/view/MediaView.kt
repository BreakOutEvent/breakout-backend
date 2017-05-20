package backend.view

import backend.model.media.Media
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import javax.validation.Valid

class MediaView() {

    var id: Long? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
    var type: String? = null

    var uploadToken: String? = null

    var sizes: List<MediaSizeView>? = null

    constructor(media: Media) : this() {
        this.id = media.id
        this.type = media.mediaType.toString()
        this.uploadToken = media.uploadToken
        this.sizes = media.sizes.map(::MediaSizeView)
    }
}
