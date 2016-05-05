package backend.view

import backend.model.posting.Posting
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class PostingView() {

    var id: Long? = null

    var text: String? = null

    @NotNull
    var date: Long? = null

    @Valid
    var postingLocation: LocationView? = null

    var media: List<MediaView>? = null

    @JsonInclude(NON_NULL)
    var uploadMediaTypes: List<String>? = null

    @Valid
    var user: BasicUserView? = null

    var comments: List<CommentView>? = null

    var likes: Int? = null

    constructor(posting: Posting) : this() {
        this.id = posting.id
        this.text = posting.text
        this.date = posting.date.toEpochSecond(ZoneOffset.UTC)
        this.postingLocation = if (posting.location != null) LocationView(posting.location!!) else null
        this.user = BasicUserView(posting.user!!.core)
        this.media = posting.media?.map { MediaView(it) }
        this.comments = posting.comments.map { CommentView(it) }
        this.likes = posting.likes.count()
    }
}
