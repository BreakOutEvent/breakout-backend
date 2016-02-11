package backend.model.user

import backend.controller.RequestBodies.PostUserBody
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.services.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {

    private val userRepository: UserRepository
    private val mailService: MailService

    @Autowired
    constructor(userRepository: UserRepository, mailService: MailService) {
        this.userRepository = userRepository
        this.mailService = mailService
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
        val token = user.createActivationToken()

        sendActivationEmail(token, user)

        return userRepository.save(user.core!!);
    }

    private fun sendActivationEmail(token: String, user: User) {
        val email = Email(
                to = listOf(EmailAddress(user.email)),
                subject = "Please activate your BreakOut Account",
                body = "Your token is $token"
        )

        mailService.send(email)
    }

    override fun save(user: User): User = userRepository.save(user.core!!)
}

