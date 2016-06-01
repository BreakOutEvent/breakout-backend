package backend.model.posting

import backend.model.user.UserCore
import java.time.LocalDateTime

interface CommentService {

    fun createComment(text: String, date: LocalDateTime, posting: Posting, user: UserCore): Comment

    fun findAll(): Iterable<Comment>

    fun getByID(id: Long): Comment?

    fun save(comment: Comment): Comment?

    fun delete(comment: Comment)

}
