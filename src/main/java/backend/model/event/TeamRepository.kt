package backend.model.event

import backend.model.posting.Posting
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface TeamRepository : CrudRepository<Team, Long> {
    fun findById(id: Long): Team

    @Query(value = "SELECT * FROM Posting WHERE user_id in (SELECT core_id FROM user_role WHERE current_team_id = :id) ORDER BY date ASC", nativeQuery = true)
    fun findPostingsById(@Param("id") id: Long): List<Posting>

    @Query(value = "SELECT * FROM Posting WHERE user_id in (SELECT core_id FROM user_role WHERE current_team_id = :id) AND latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY date ASC", nativeQuery = true)
    fun findLocationPostingsById(@Param("id") id: Long): List<Posting>
}
