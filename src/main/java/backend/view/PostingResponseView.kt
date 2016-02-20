package backend.view

import backend.model.posting.Posting
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class PostingResponseView() {

    var id: Long? = null

    var text: String? = null

    var date: Long? = null

    @Valid
    var postingLocation: PostingResponseView.Coords? = null

    var media: List<MediaView>? = null

    @Valid
    var user: UserView? = null

    constructor(posting: Posting) : this() {
        this.id = posting.id
        this.text = posting.text
        this.date = posting.date.toEpochSecond(ZoneOffset.UTC)
        this.postingLocation = PostingResponseView.Coords()
        this.postingLocation?.latitude = posting.postLocation?.latitude
        this.postingLocation?.longitude = posting.postLocation?.longitude
        this.user = UserView(posting.user!!.core)
        this.media = posting.media?.map { MediaView(it) }
    }

    class Coords() {

        @NotNull
        var latitude: Double? = null

        @NotNull
        var longitude: Double? = null
    }

}
