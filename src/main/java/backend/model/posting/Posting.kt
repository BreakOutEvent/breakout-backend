package backend.model.posting

import backend.model.BasicEntity
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.user.UserCore
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class Posting() : BasicEntity() {

    var text: String? = null

    lateinit var date: LocalDateTime

    @Embedded
    @AttributeOverrides(
            AttributeOverride(name = "latitude", column = Column(nullable = true)),
            AttributeOverride(name = "longitude", column = Column(nullable = true))
    )
    var postLocation: Coord? = null

    var distance: Double? = null

    @ManyToOne
    var user: UserCore? = null

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var media: MutableList<Media>? = ArrayList()


    constructor(text: String?, postLocation: Coord?, user: UserCore, media: MutableList<Media>?, distance: Double?) : this() {
        this.text = text
        this.date = LocalDateTime.now()
        this.postLocation = postLocation
        this.user = user
        this.media = media
        this.distance = distance
    }

}
