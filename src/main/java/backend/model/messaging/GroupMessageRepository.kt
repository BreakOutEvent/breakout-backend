package backend.model.messaging

import org.springframework.data.repository.CrudRepository

interface GroupMessageRepository : CrudRepository<GroupMessage, Long> {
    fun findById(id: Long): GroupMessage
}
