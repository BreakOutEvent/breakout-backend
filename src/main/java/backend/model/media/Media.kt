package backend.model.media

import backend.model.BasicEntity
import backend.view.MediaView
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity
class Media : BasicEntity {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(mediaType: MediaType, url: String?) {
        this.mediaType = mediaType

        this.url = url
    }

    constructor(mediaView: MediaView) {
        this.mediaType = MediaType.valueOf(mediaView.type.toUpperCase())

        this.url = mediaView.url
    }


    @Enumerated(EnumType.STRING)
    lateinit var mediaType: MediaType

    var url: String? = null
}
