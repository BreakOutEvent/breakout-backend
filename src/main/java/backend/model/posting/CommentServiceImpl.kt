package backend.model.posting

import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentServiceImpl @Autowired constructor(val repository: CommentRepository) : CommentService {
    override fun findByPosting(posting: Posting): List<Comment> = repository.findByPosting(posting)

    override fun createComment(text: String, date: LocalDateTime, posting: Posting, user: UserCore): Comment {
        val comment: Comment = Comment(text, date, posting, user)
        return repository.save(comment)
    }

    override fun save(comment: Comment): Comment = repository.save(comment)!!

    override fun findAll(): Iterable<Comment> = repository.findAll()

    override fun getByID(id: Long): Comment? = repository.findById(id)
}
