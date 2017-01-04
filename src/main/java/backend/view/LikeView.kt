package backend.view

import backend.model.posting.Like
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class LikeView() {

    var id: Long? = null

    @NotNull
    var date: Long? = null

    @Valid
    var user: BasicUserView? = null

    constructor(like: Like) : this() {
        this.id = like.id
        this.date = like.date.toEpochSecond(ZoneOffset.UTC)
        this.user = BasicUserView(like.user!!.account)
    }
}
