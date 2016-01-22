package backend.model.user

import backend.controller.RequestBodies.PostUserBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {

    private val userRepository: UserRepository

    @Autowired
    constructor(userRepository: UserRepository) {
        this.userRepository = userRepository
    }

    override fun getUserById(id: Long): User? = userRepository.findOne(id)

    override fun getUserByEmail(email: String): User? = userRepository.findByEmail(email);

    override fun getAllUsers(): MutableIterable<UserCore>? = userRepository.findAll()

    override fun exists(id: Long) = userRepository.exists(id)

    override fun exists(email: String) = userRepository.existsByEmail(email)

    @Deprecated("Remains for testing purposes")
    override fun create(body: PostUserBody): User? {

        val user = User.create(body.email!!, body.password!!)
        user.firstname = body.firstname
        user.lastname = body.lastname
        user.gender = body.gender
        user.isBlocked = false

        return userRepository.save(user.core);
    }

    override fun create(email: String, password: String): User {
        if (this.exists(email)) throw Exception("user with email $email already exists")
        val user = User.create(email, password)
        user.isBlocked = false
        return userRepository.save(user.core!!);
    }

    override fun save(user: User): User = userRepository.save(user.core!!)
}

