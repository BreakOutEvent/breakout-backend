package backend.model.user

interface UserService {
    fun getUserById(id: Long): User?
    fun getUserByEmail(email: String): User?
    fun getAllUsers(): MutableIterable<UserCore>?;

    fun create(email: String, password: String): User
    fun create(email: String, password: String, f: User.() -> Unit)

    fun save(user: User): User?

    fun exists(id: Long): Boolean
    fun exists(email: String): Boolean

    fun activate(user: User, token: String)
}
