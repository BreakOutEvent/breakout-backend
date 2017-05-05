package backend.view

import backend.model.posting.Posting
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class PostingView() {

    var id: Long? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
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

    var hasLiked: Boolean = false

    var hashtags: List<String> = arrayListOf()

    var proves: ChallengeView? = null


    constructor(posting: Posting) : this() {
        this.id = posting.id
        this.text = posting.text
        this.hashtags = posting.hashtags.map { it.value }
        this.date = posting.date.toEpochSecond(ZoneOffset.UTC)
        this.postingLocation = posting.location?.let(::LocationView)
        this.user = BasicUserView(posting.user!!.account)
        this.media = posting.media.map(::MediaView)
        this.comments = posting.comments.map(::CommentView)
        this.likes = posting.likes.count()
        this.hasLiked = posting.hasLiked
        this.proves = posting.challenge?.let(::ChallengeView)
    }
}
