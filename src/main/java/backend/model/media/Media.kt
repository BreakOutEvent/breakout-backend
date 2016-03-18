package backend.model.media

import backend.model.BasicEntity
import java.util.*
import javax.persistence.*

@Entity
class Media : BasicEntity {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    @Enumerated(EnumType.STRING)
    var mediaType: MediaType? = null

    @OrderColumn
    @OneToMany(mappedBy = "media", fetch = FetchType.EAGER, orphanRemoval = true)
    var sizes: MutableList<MediaSize>? = ArrayList()

    @Transient
    var uploadToken: String? = null

    constructor(type: String) : this() {
        this.mediaType = MediaType.valueOf(type.toUpperCase())
    }

}
