package backend.model.event

import backend.model.misc.Coords
import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PostServiceImpl @Autowired constructor(val repository: PostRepository) : PostService {

    override fun save(post: Post): Post = repository.save(post)!!

    override fun findAll(): Iterable<Post> = repository.findAll()

    override fun createPost(text: String, postLocation: Coords, user: UserCore): Post {
        val post = Post(text, postLocation, user)
        return repository.save(post)
    }

    override fun getByID(id: Long): Post? {
        return repository.findById(id)
    }
}
