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

    fun findAllByUserId(userId: Long): List<Posting>

    @Query("select distinct p from Posting p inner join p.comments c where c.user.id = :userId")
    fun findAllCommentedByUser(@Param("userId") userId: Long): List<Posting>

    @Query("select distinct p.id from Posting p inner join p.comments c where c.id = :commentId")
    fun findCommentsById(@Param("commentId") commentId: Long): Comment?

    @Query("select p from Posting p inner join p.likes l where l.user.id = :userId")
    fun findAllLikedByUser(@Param("userId") userId: Long): List<Posting>


    @Query("select p from Posting p where p.challenge = :challengeId")
    fun findAllByChallengeId(@Param("challengeId") challengeId: Long): List<Posting>

    @Query("""
        select *
        from posting
        where team_id = :id
        order by id desc
        limit 1
    """, nativeQuery = true)
    fun findLastPostingByTeamId(@Param("id") id: Long): Posting?
}
