package backend.model.posting

import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LikeServiceImpl @Autowired constructor(val repository: LikeRepository) : LikeService {

    override fun findAllByPosting(posting: Posting): List<Like> = repository.findByPosting(posting)

    @Transactional
    override fun createLike(date: LocalDateTime, posting: Posting, user: UserCore): Like {
        val like: Like = Like(date, posting, user)
        repository.save(like)
        posting.likes.add(like)
        return like
    }

    override fun save(like: Like): Like = repository.save(like)!!

    override fun findAll(): Iterable<Like> = repository.findAll()

    override fun getByID(id: Long): Like? = repository.findById(id)
}
