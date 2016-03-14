package backend.view

import backend.model.posting.Posting
import java.time.ZoneOffset
import javax.validation.Valid

class PostingResponseView() {

    var id: Long? = null

    var text: String? = null

    var date: Long? = null

    var distance: Double? = null

    @Valid
    var postingLocation: CoordView? = null

    var media: List<MediaView>? = null

    @Valid
    var user: UserView? = null

    constructor(posting: Posting) : this() {
        this.id = posting.id
        this.text = posting.text
        this.date = posting.date.toEpochSecond(ZoneOffset.UTC)
        this.postingLocation = CoordView(posting.location?.toCoord())
        this.user = UserView(posting.user!!.core)
        this.media = posting.media?.map { MediaView(it) }
        this.distance = posting.distance
    }


}
