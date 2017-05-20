package backend.view.posting

import backend.model.posting.Posting
import backend.view.MediaView

class PostingUserView() {

    var id: Long? = null
    var firstname: String? = null
    var lastname: String? = null
    var profilePic: MediaView? = null
    var participant: MutableMap<String, Any>? = null

    constructor(posting: Posting) : this() {
        id = posting.user?.id
        firstname = posting.user?.firstname
        lastname = posting.user?.lastname
        profilePic = posting.user?.profilePic?.let {
            return@let MediaView(it)
        }
        posting.team?.let {
            participant = mutableMapOf(
                    "eventId" to it.event.id as Long,
                    "teamId" to it.id as Any,
                    "teamName" to it.name as Any
            )
        }
    }
}