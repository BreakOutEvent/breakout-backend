package backend.model.posting

import org.springframework.data.repository.CrudRepository

interface PostingRepository : CrudRepository<Posting, Long> {
    fun findById(id: Long): Posting
}
