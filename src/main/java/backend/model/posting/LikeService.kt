package backend.model.posting

import backend.model.user.UserCore
import java.time.LocalDateTime

interface LikeService {

    fun findAllByPosting(posting: Posting): List<Like>

    fun createLike(date: LocalDateTime, posting: Posting, user: UserCore): Like

    fun findAll(): Iterable<Like>

    fun getByID(id: Long): Like?

    fun save(like: Like): Like?

}
