@file:JvmName("UserRepository")

package backend.model.user

import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<UserCore, Long> {
    fun findByEmail(email: String): User
}
