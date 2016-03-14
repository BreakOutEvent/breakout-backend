package backend.model.posting

import backend.model.BasicEntity
import backend.model.location.Location
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.user.UserCore
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.PERSIST

@Entity
class Posting : BasicEntity {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    var text: String? = null

    lateinit var date: LocalDateTime

    @OneToOne(cascade = arrayOf(PERSIST))
    var location: Location? = null

    var distance: Double? = null

    @ManyToOne
    var user: UserCore? = null

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var media: MutableList<Media>? = ArrayList()


    constructor(text: String?, location: Location?, user: UserCore, media: MutableList<Media>?, distance: Double?) : this() {
        this.text = text
        this.date = LocalDateTime.now()
        this.location = location
        this.user = user
        this.media = media
        this.distance = distance
    }

}
