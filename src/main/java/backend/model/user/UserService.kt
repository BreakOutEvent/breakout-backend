package backend.model.user

import backend.configuration.CustomUserDetails

interface UserService {

    fun getUserFromCustomUserDetails(customUserDetails: CustomUserDetails): User

    fun getUserById(id: Long): User?
    fun getUserByEmail(email: String): User?
    fun getAllUsers(): Iterable<UserCore>

    fun create(email: String, password: String): User
    fun create(email: String, password: String, f: User.() -> Unit): User

    fun save(user: User): User

    fun exists(id: Long): Boolean
    fun exists(email: String): Boolean

    fun activate(user: User, token: String)

    fun getUserByActivationToken(token: String): User?

    fun requestReset(emailString: String)

    fun resetPassword(emailString: String, password: String, token: String)

    fun searchByString(search: String): List<UserCore>
}
