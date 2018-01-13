package backend.view.posting

import backend.model.challenges.ChallengeProofProjection
import backend.model.posting.Posting
import backend.view.CommentView
import backend.view.MediaView
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.Valid
import javax.validation.constraints.NotNull


class PostingResponseView() {

    var id: Long? = null

    var text: String? = null

    @NotNull
    var date: Long? = null

    @Valid
    var postingLocation: PostingLocationView? = null

    var media: MediaView? = null

    @JsonInclude(NON_NULL)
    var uploadMediaTypes: List<String>? = null

    @Valid
    var user: PostingUserView? = null

    var comments: List<CommentView>? = null

    var likes: Int? = null

    var hasLiked: Boolean = false

    var hashtags: List<String> = arrayListOf()

    var proves: PostingChallengeView? = null


    constructor(posting: Posting, challenge: ChallengeProofProjection?) : this() {
        this.id = posting.id
        this.text = posting.text
        this.hashtags = posting.hashtags.map { it.value }
        this.date = posting.date.toEpochSecond(java.time.ZoneOffset.UTC)
        this.postingLocation = posting.location?.let(::PostingLocationView)
        this.user = PostingUserView(posting)
        this.media = posting.media?.let(::MediaView)
        this.comments = posting.comments.map(::CommentView)
        this.likes = posting.likes.count()
        this.hasLiked = posting.hasLiked
        this.proves = challenge?.let(::PostingChallengeView)
    }
}


