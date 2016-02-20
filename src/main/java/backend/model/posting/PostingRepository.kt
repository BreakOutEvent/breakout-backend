package backend.model.posting

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PostingRepository : CrudRepository<Posting, Long> {
    fun findById(id: Long): Posting

    @Query("from Posting where id > :id order by id desc")
    fun findAllSince(@Param("id") id: Long): Iterable<Posting>
}
