@file:JvmName("UserRepository")

package backend.model.user

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<UserCore, Long> {
    fun findByEmail(email: String): User

    fun findByActivationToken(token: String): User

    @Query("select case when count(e) > 0 then true else false end from UserCore e where e.email = ?1")
    fun existsByEmail(email: String): Boolean
}
