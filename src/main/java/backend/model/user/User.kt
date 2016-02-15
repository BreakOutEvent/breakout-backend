package backend.model.user

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

interface User {

    var email: String
    var passwordHash: String
    var isBlocked: Boolean

    val core: UserCore?
    var firstname: String?
    var lastname: String?
    var gender: String?

    fun addRole(clazz: Class<out UserRole>): UserRole
    fun getRole(clazz: Class<out UserRole>): UserRole?
    fun hasRole(clazz: Class<out UserRole>): Boolean
    fun removeRole(clazz: Class<out UserRole>): UserRole?

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
