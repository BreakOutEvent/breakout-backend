package backend.model.post

import org.springframework.data.repository.CrudRepository

interface PostRepository : CrudRepository<Post, Long> {
    fun findById(id: Long): Post
}
