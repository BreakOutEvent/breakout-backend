@file:JvmName("UserRepository")

package backend.model.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<UserAccount, Long> {
    fun findByEmail(email: String): User

    fun findByActivationToken(token: String): User

    @Query("select case when count(e) > 0 then true else false end from UserAccount e where e.email = ?1")
    fun existsByEmail(email: String): Boolean

    @Query("from UserAccount u where u.firstname like concat('%',:search,'%') or u.lastname like concat('%',:search,'%') or u.email like concat('%',:search,'%')")
    fun searchByString(@Param("search") search: String): List<UserAccount>

    @Query("from Sponsor s")
    fun findAllSponsors(): Iterable<Sponsor>

    @Query("select u from UserAccount u join u.blockedBy b where b.id = :userid")
    fun findAllUsersBlockedByUser(@Param("userid") userId: Long): List<UserAccount>

    @Query("select u from UserAccount u join u.userRoles r where KEY(r) = :role")
    fun findAllUsersByRole(@Param("role") role: Class<out UserRole>): List<UserAccount>

}
