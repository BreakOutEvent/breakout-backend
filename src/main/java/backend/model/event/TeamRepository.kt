package backend.model.event

import backend.model.posting.Posting
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface TeamRepository : CrudRepository<Team, Long> {
    fun findById(id: Long): Team

    //@Query(value = "SELECT * FROM Posting WHERE user_id in (SELECT core_id FROM user_role WHERE current_team_id = :id) ORDER BY date ASC", nativeQuery = true)
    @Query("from Posting p inner join p.user u inner join u.userRoles r where r.currentTeam.id = :id order by p.date asc")
    fun findPostingsById(@Param("id") id: Long): List<Posting>

    //@Query(value = "SELECT * FROM Posting WHERE user_id in (SELECT core_id FROM user_role WHERE current_team_id = :id) AND latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY date ASC", nativeQuery = true)
    @Query("from Posting p inner join p.user u inner join u.userRoles r where r.currentTeam.id = :id and latitude is not null and longitude is not null order by p.date asc")
    fun findLocationPostingsById(@Param("id") id: Long): List<Posting>
}
