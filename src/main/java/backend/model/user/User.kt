package backend.model.user

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

}
