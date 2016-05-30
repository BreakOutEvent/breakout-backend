package backend.model.media

import backend.model.BasicEntity
import backend.util.getSignedJwtToken
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

    @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "media", fetch = FetchType.EAGER, orphanRemoval = true)
    var sizes: MutableList<MediaSize> = ArrayList()

    @Transient
    var uploadToken: String? = null

    fun generateSignedUploadToken(secret: String) {
        val subject = this.id?.toString() ?: throw Exception("Can't generate upload token for object without id")
        this.uploadToken = getSignedJwtToken(secret, subject)
    }

    constructor(type: String) : this() {
        this.mediaType = MediaType.valueOf(type.toUpperCase())
    }

    constructor(type: MediaType) : this() {
        this.mediaType = type
    }

    @PreRemove
    fun preRemove() {
        this.sizes.clear()
    }
}
