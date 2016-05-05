package backend.model.posting

import org.springframework.data.repository.CrudRepository

interface CommentRepository : CrudRepository<Comment, Long> {
    fun findById(id: Long): Comment
}
