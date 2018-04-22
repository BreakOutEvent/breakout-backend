package backend.model.messaging

import org.springframework.data.jpa.repository.JpaRepository

interface GroupMessageRepository : JpaRepository<GroupMessage, Long> {
    fun findById(id: Long): GroupMessage
}
