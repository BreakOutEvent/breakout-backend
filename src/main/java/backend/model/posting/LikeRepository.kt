package backend.model.posting

import org.springframework.data.repository.CrudRepository

interface LikeRepository : CrudRepository<Like, Long> {
    fun findById(id: Long): Like
    fun findByPosting(posting: Posting): List<Like>
}
