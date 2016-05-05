package backend.model.user

import backend.model.media.Media
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.reflect.KClass

interface User {

    var email: String
    var passwordHash: String
    var isBlocked: Boolean

    val core: UserCore
    var firstname: String?
    var lastname: String?
    var gender: String?
    var profilePic: Media

    fun <T : UserRole> addRole(clazz: KClass<T>): T
    fun <T : UserRole> getRole(clazz: KClass<T>): T?
    fun <T : UserRole> hasRole(clazz: KClass<T>): Boolean
    fun <T : UserRole> removeRole(clazz: KClass<T>): T?

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

    fun isSameUserAs(user: User) = this.core.id == user.core.id

    fun setNewPassword(password: String, token: String)
}
