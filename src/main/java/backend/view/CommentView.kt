package backend.view

import backend.model.posting.Comment
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserAccount
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.ZoneOffset
import javax.validation.Valid
import javax.validation.constraints.NotNull

class CommentView() {

    var id: Long? = null

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

class CommentUserView() {

    var firstname: String? = null
    var lastname: String? = null
    var profilePic: MediaView? = null


    constructor(user: UserAccount?) : this() {
        firstname = user?.firstname
        lastname = user?.lastname
        profilePic = user?.profilePic?.let {
            return@let MediaView(it)
        }
    }
}
