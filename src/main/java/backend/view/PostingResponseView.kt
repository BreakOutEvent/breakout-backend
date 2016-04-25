package backend.view

import backend.model.posting.Posting
import java.time.ZoneOffset
import javax.validation.Valid

class PostingResponseView() {

    var id: Long? = null

    var text: String? = null

    var date: Long? = null

    @Valid
    var postingLocation: LocationView? = null

    var media: List<MediaView>? = null

    @Valid
    var user: BasicUserView? = null

    constructor(posting: Posting) : this() {
        this.id = posting.id
        this.text = posting.text
        this.date = posting.date.toEpochSecond(ZoneOffset.UTC)
        this.postingLocation = if (posting.location != null) LocationView(posting.location!!) else null
        this.user = BasicUserView(posting.user!!.core)
        this.media = posting.media?.map { MediaView(it) }
    }


}
