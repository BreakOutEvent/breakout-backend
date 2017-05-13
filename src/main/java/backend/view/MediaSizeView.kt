package backend.view

import backend.model.media.MediaSize
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import javax.validation.constraints.NotNull

class MediaSizeView() {

    var id: Long? = null

    @NotNull
    @SafeHtml(whitelistType = NONE)
    var url: String? = null

    @NotNull
    var width: Int? = null

    @NotNull
    var height: Int? = null

    @NotNull
    var length: Int? = null

    @NotNull
    var size: Long? = null

    @NotNull
    @SafeHtml(whitelistType = NONE)
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
