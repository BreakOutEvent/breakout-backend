package backend.model.event

import backend.model.misc.Coords
import backend.model.user.User

interface PostService {
    fun createPost(text: String, postLocation: Coords, user: User): Post
    fun findAll(): Iterable<Post>
    fun save(post: Post): Post?

}
