package backend.view

import backend.model.media.Media
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import javax.validation.Valid

class MediaView(id: Long?, type: String, url: String?) {

    constructor(media: Media) : this(media.id, media.mediaType.name, media.url)

    var id: Long? = id

    @Valid
    @SafeHtml(whitelistType = NONE)
    var type: String = type

    var url: String? = url
}
