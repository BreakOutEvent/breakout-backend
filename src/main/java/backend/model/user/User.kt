package backend.model.user

import backend.Blockable
import backend.model.media.Media
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.reflect.KClass

interface User: Blockable {

    var email: String
    var passwordHash: String
    var isBlocked: Boolean
    var preferredLanguage: Language

    val account: UserAccount
    var firstname: String?
    var lastname: String?
    var gender: String?
    var profilePic: Media?
    var notificationToken: String?

    fun <T : UserRole> addRole(clazz: KClass<T>): T
    fun <T : UserRole> getRole(clazz: KClass<T>): T?
    fun <T : UserRole> hasRole(clazz: KClass<T>): Boolean
    fun <T : UserRole> removeRole(clazz: KClass<T>): T?

    companion object {
        fun create(email: String, password: String): User {
            val user = UserAccount()
            user.email = email
            user.passwordHash = BCryptPasswordEncoder().encode(password)
            return user
        }
    }

    fun activate(token: String)
    fun isActivationTokenCorrect(token: String): Boolean
    fun createActivationToken(): String
    fun isActivated(): Boolean

    fun setNewPassword(password: String, token: String)

    override fun isBlockedBy(userId: Long?): Boolean {
        return false // TODO:
    }

}

enum class Language {
    DE, EN
}
