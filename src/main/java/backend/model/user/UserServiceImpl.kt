package backend.model.user

import backend.controller.RequestBodies.PostUserBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceImpl: UserService {

    private val userRepository: UserRepository

    @Autowired
    constructor(userRepository: UserRepository) {
        this.userRepository = userRepository
    }

    override fun getUserById(id: Long): User? = userRepository.findOne(id)

    override fun getUserByEmail(email: String): User? = userRepository.findByEmail(email);

    override fun getAllUsers(): MutableIterable<UserCore>? = userRepository.findAll()

    override fun create(body: PostUserBody): User? {
        val user = UserCore()
        user.email = body.email!!
        user.firstname = body.firstname!!
        user.lastname = body.lastname!!
        user.gender = body.gender!!
        user.isBlocked = false;
        user.passwordHash = BCryptPasswordEncoder().encode(body.password);
        return userRepository.save(user);
    }
}

