package backend.view

import backend.model.posting.Comment
import backend.view.user.CommentUserView
import org.hibernate.validator.constraints.SafeHtml
import org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class CommentView() {

    var id: Long? = null

    @Valid
    @SafeHtml(whitelistType = NONE)
    lateinit var text: String

    @NotNull
    var date: Long? = null

    @Valid
    var user: CommentUserView? = null

    constructor(comment: Comment) : this() {
        this.id = comment.id
        this.text = comment.text
        this.date = comment.date.toEpochSecond(ZoneOffset.UTC)
        this.user = CommentUserView(comment.user!!.account)
    }
}

