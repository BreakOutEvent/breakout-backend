package backend.model.posting

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PostingRepository : CrudRepository<Posting, Long> {
    fun findById(id: Long): Posting

    @Query("from Posting where id > :id order by id desc")
    fun findAllSince(@Param("id") id: Long): Iterable<Posting>

    @Query("select p from Posting p inner join p.hashtags h where h.value = :hashtag order by p.id desc")
    fun findByHashtag(@Param("hashtag") hashtag: String): Iterable<Posting>

    fun findAll(pageable: Pageable): Iterable<Posting>
}
