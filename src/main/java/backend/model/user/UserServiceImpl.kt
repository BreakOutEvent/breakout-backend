package backend.model.user

import backend.controller.exceptions.ConflictException
import backend.exceptions.DomainException
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.services.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {

    private val userRepository: UserRepository
    private val mailService: MailService

    @Value("\${org.breakout.api.host}")
    private lateinit var BASEURL: String

    @Value("\${org.breakout.api.port}")
    private lateinit var PORT: String

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

    override fun create(email: String, password: String): User {
        if (this.exists(email)) throw ConflictException("user with email $email already exists")
        val user = User.create(email, password)
        val token = user.createActivationToken()

        sendActivationEmail(token, user)

        return userRepository.save(user.core!!);
    }

    override fun activate(user: User, token: String) {
        if(user.isActivated()) throw DomainException("User already is activated")
        else if (!user.isActivationTokenCorrect(token)) throw DomainException("Incorrect activation token")
        else user.activate(token)
        this.save(user)
    }

    private fun sendActivationEmail(token: String, user: User) {
        val activationUrl = createActivationUrl(token, user)
        val email = Email(
                to = listOf(EmailAddress(user.email)),
                subject = "Please activate your BreakOut Account",
                body = "Your token is $token<br/>Please click the following link: $activationUrl"
        )
        mailService.send(email)
    }

    private fun createActivationUrl(token: String, user: User): String {
        return "http://$BASEURL:$PORT/activation?token=$token&email=${user.email}"
    }

    override fun save(user: User): User = userRepository.save(user.core!!)

    override fun create(email: String, password: String, f: User.() -> Unit) {
        val user = this.create(email, password)
        f.invoke(user)
        this.save(user)
    }
}

