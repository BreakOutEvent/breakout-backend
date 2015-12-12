package backend.model.user

interface User {

    val core: UserCore?
    var firstname: String
    var lastname: String
    var email: String
    var isBlocked: Boolean
    var passwordHash: String
    var gender: String

    fun addRole(clazz: Class<out UserRole>): UserRole
    fun getRole(clazz: Class<out UserRole>): UserRole?
    fun hasRole(clazz: Class<out UserRole>): Boolean
    fun removeRole(clazz: Class<out UserRole>): UserRole?

}
