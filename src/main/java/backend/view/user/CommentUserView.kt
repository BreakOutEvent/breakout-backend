package backend.view.user

import backend.model.user.UserAccount
import backend.view.MediaView

class CommentUserView() {

    var id: Long? = null
    var firstname: String? = null
    var lastname: String? = null
    var profilePic: MediaView? = null


    constructor(user: UserAccount?) : this() {
        id = user?.id
        firstname = user?.firstname
        lastname = user?.lastname
        profilePic = user?.profilePic?.let {
            return@let MediaView(it)
        }
    }
}