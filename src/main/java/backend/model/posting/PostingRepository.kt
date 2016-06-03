package backend.model.posting

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PostingRepository : CrudRepository<Posting, Long> {
    fun findById(id: Long): Posting

    @Query("select p.id from Posting p where p.id > :id order by p.id desc")
    fun findAllIdsSince(@Param("id") id: Long): List<Long>

    @Query("select p from Posting p inner join p.hashtags h where h.value = :hashtag group by concat(lower(h.value), ' - ', p.id) order by p.id desc")
    fun findByHashtag(@Param("hashtag") hashtag: String): List<Posting>

    fun findAllByOrderByIdDesc(pageable: Pageable): List<Posting>

    fun findAllByOrderByIdDesc(): List<Posting>

    @Query("from Posting where id in :ids order by id desc")
    fun findAllByIds(@Param("ids") ids: List<Long>): Iterable<Posting>
}
