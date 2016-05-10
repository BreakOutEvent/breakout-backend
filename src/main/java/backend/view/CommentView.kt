package backend.view

import backend.model.posting.Comment
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class CommentView() {

    var id: Long? = null

    lateinit var text: String

    @NotNull
    var date: Long? = null

    @Valid
    var user: BasicUserView? = null

    constructor(comment: Comment) : this() {
        this.id = comment.id
        this.text = comment.text
        this.date = comment.date.toEpochSecond(ZoneOffset.UTC)
        this.user = BasicUserView(comment.user!!.core)
    }
}
