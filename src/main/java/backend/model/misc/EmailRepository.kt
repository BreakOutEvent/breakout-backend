package backend.model.misc

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EmailRepository : CrudRepository<Email, Long> {

    @Query("select e from Email e inner join e.to r where r.value = :email")
    fun findByReceipient(@Param("email") email: String): List<Email>

    fun findByIsSent(isSend: Boolean): List<Email>

}