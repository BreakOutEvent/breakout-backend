package backend.model.misc

import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository : JpaRepository<Email, Long> {

    fun findByIsSent(isSend: Boolean): List<Email>

}