package backend.model.posting

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PostingRepository : CrudRepository<Posting, Long> {
    fun findById(id: Long): Posting

    @Query("select p from Posting p inner join p.hashtags h where h.value = :hashtag order by p.id desc")
    fun findByHashtag(@Param("hashtag") hashtag: String, pageable: Pageable): List<Posting>

    fun findAllByOrderByIdDesc(pageable: Pageable): List<Posting>

    fun findByTeamEventIdInOrderByIdDesc(eventIdList: List<Long>, pageable: Pageable): List<Posting>

    @Query("select p from Posting p where p.reported = true")
    fun findReported(): List<Posting>
}
