package backend.view.posting

import backend.model.challenges.Challenge
import backend.model.posting.Posting
import backend.model.removeBlockedBy
import backend.view.CommentView
import backend.view.LocationView
import backend.view.MediaView
import backend.view.challenge.ChallengeView
import backend.view.user.BasicUserView
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

    var media: MediaView? = null

    @Valid
    var user: BasicUserView? = null

    var comments: List<CommentView>? = null

    var likes: Int? = null

    var hasLiked: Boolean = false

    var hashtags: List<String> = arrayListOf()

    var proves: ChallengeView? = null

    constructor(posting: Posting, challenge: Challenge?, userId: Long?) : this() {
        this.id = posting.id
        this.text = posting.text
        this.hashtags = posting.hashtags.map { it.value }
        this.date = posting.date.toEpochSecond(ZoneOffset.UTC)
        this.postingLocation = posting.location?.let(::LocationView)
        this.user = BasicUserView(posting.user!!.account)
        this.media = posting.media?.let(::MediaView)
        this.comments = posting.comments.removeBlockedBy(userId).map(::CommentView)
        this.likes = posting.likes.count()
        this.hasLiked = posting.hasLiked
        this.proves = challenge?.let(::ChallengeView)
    }
}
