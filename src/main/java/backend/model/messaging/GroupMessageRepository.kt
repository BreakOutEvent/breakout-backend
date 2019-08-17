package backend.model.messaging

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface GroupMessageRepository : CrudRepository<GroupMessage, Long> {
    fun findById(id: Long): GroupMessage

    @Query("select distinct g from GroupMessage g inner join g.messages m where m.creator.id = :userId")
    fun findWhereUserHasSentMessages(@Param("userId") userId: Long): List<GroupMessage>
}
