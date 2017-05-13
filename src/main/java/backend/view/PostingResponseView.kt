package backend.view

import backend.model.challenges.ChallengeProofProjection
import backend.model.location.Location
import backend.model.posting.Posting
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull


class PostingResponseView() {

    var id: Long? = null

    var text: String? = null

    @NotNull
    var date: Long? = null

    @Valid
    var postingLocation: PostingLocationView? = null

    var media: List<MediaView>? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
        this.date = posting.date.toEpochSecond(ZoneOffset.UTC)
        this.postingLocation = posting.location?.let(::PostingLocationView)
        this.user = PostingUserView(posting)
        this.media = posting.media.map(::MediaView)
        this.comments = posting.comments.map(::CommentView)
        this.likes = posting.likes.count()
        this.hasLiked = posting.hasLiked
        this.proves = challenge?.let(::PostingChallengeView)
    }
}

class PostingUserView() {

    var firstname: String? = null
    var lastname: String? = null
    var profilePic: MediaView? = null
    var participant: MutableMap<String, Any>? = null

    constructor(posting: Posting) : this() {
        firstname = posting.user?.firstname
        lastname = posting.user?.lastname
        profilePic = posting.user?.profilePic?.let {
            return@let MediaView(it)
        }
        posting.team?.let {
            participant = mutableMapOf(
                    "teamId" to it.id as Any,
                    "teamName" to it.name as Any
            )
        }
    }
}

class PostingChallengeView {

    var id: Long? = null
    var status: String? = null
    var amount: Double? = null
    var description: String? = null

    /**
     * no-args constructor for Jackson
     */
    constructor()

    constructor(challenge: ChallengeProofProjection) {
        this.id = challenge.getId()
        this.status = challenge.getStatus()
        this.amount = challenge.getAmount().numberStripped.toDouble()
        this.description = challenge.getDescription()
    }
}

class PostingLocationView() {

    @NotNull
    var latitude: Double? = null

    @NotNull
    var longitude: Double? = null

    var distance: Double? = null

    var locationData: Map<String, String> = mapOf()

    @NotNull
    var date: Long? = null

    constructor(location: Location): this() {
        this.latitude = location.coord.latitude
        this.longitude = location.coord.longitude
        this.distance = location.distance
        this.date = location.date.toEpochSecond(ZoneOffset.UTC)
        this.locationData = location.locationData
    }
}


