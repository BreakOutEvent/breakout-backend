package backend.model.posting

import backend.controller.exceptions.ConflictException
import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LikeServiceImpl @Autowired constructor(val repository: LikeRepository) : LikeService {

    override fun findAllByPosting(posting: Posting): List<Like> = repository.findByPosting(posting)

    @Transactional
    override fun createLike(date: LocalDateTime, posting: Posting, user: UserCore): Like {
        val like: Like = Like(date, posting, user)
        try {
            repository.save(like)
        } catch(e: DataIntegrityViolationException) {
            throw ConflictException("like for posting ${posting.id} and user ${user.id} already exists")
        }
        posting.likes.add(like)
        return like
    }

    override fun save(like: Like): Like = repository.save(like)!!

    override fun findAll(): Iterable<Like> = repository.findAll()

    override fun getByID(id: Long): Like? = repository.findById(id)

    @Transactional
    override fun deleteLike(posting: Posting, user: UserCore) {
        val like = posting.likes.filter { it.user!!.core.id == user.id }.firstOrNull()
        if (like != null) {
            repository.delete(like)
        }
        posting.likes.remove(like)
    }
}
