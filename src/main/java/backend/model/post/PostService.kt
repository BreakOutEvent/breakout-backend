package backend.model.post

import backend.model.misc.Coords
import backend.model.user.UserCore

interface PostService {
    fun createPost(text: String?, postLocation: Coords?, user: UserCore, media: MutableList<Media>?): Post
    fun findAll(): Iterable<Post>
    fun getByID(id: Long): Post?
    fun save(post: Post): Post?

}
