package backend.model.user

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.reflect.KClass

interface User {

    var email: String
    var passwordHash: String
    var isBlocked: Boolean

    val core: UserCore?
    var firstname: String?
    var lastname: String?
    var gender: String?

    @Deprecated("Remains for compatibility purposes")
    fun addRole(clazz: Class<out UserRole>): UserRole

    @Deprecated("Remains for compatibility purposes")
    fun getRole(clazz: Class<out UserRole>): UserRole?

    @Deprecated("Remains for compatibility purposes")
    fun hasRole(clazz: Class<out UserRole>): Boolean

    @Deprecated("Remains for compatibility purposes")
    fun removeRole(clazz: Class<out UserRole>): UserRole?

    fun <T: UserRole> addRole(clazz: KClass<T>): T
    fun <T: UserRole> getRole(clazz: KClass<T>): T?
    fun <T: UserRole> hasRole(clazz: KClass<T>): Boolean
    fun <T: UserRole> removeRole(clazz: KClass<T>): T?

    companion object {
        fun create(email: String, password: String): User {
            val user = UserCore()
            user.email = email
            user.passwordHash = BCryptPasswordEncoder().encode(password)
            return user
        }
    }

    fun activate(token: String)
    fun isActivationTokenCorrect(token: String): Boolean
    fun createActivationToken(): String
    fun isActivated(): Boolean
}
