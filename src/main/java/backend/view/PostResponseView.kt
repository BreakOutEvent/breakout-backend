package backend.view

import backend.model.post.Post
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class PostResponseView() {

    var id: Long? = null

    var text: String? = null

    var date: Long? = null

    @Valid
    var postLocation: PostResponseView.Coords? = null

    var media: List<MediaView>? = null

    @Valid
    var user: UserView? = null

    constructor(post: Post) : this() {
        this.id = post.id
        this.text = post.text
        this.date = post.date.toEpochSecond(ZoneOffset.UTC)
        this.postLocation = PostResponseView.Coords()
        this.postLocation?.latitude = post.postLocation?.latitude
        this.postLocation?.longitude = post.postLocation?.longitude
        this.user = UserView(post.user!!.core)
        this.media = post.media?.map { MediaView(it) }
    }

    class Coords() {

        @NotNull
        var latitude: Double? = null

        @NotNull
        var longitude: Double? = null
    }

}
