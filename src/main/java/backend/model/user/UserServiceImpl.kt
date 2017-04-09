package backend.model.user

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.services.ConfigurationService
import backend.services.mail.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {

    private val userRepository: UserRepository
    private val mailService: MailService
    private val host: String

    @Autowired
    constructor(userRepository: UserRepository, mailService: MailService, configurationService: ConfigurationService) {
        this.userRepository = userRepository
        this.mailService = mailService
        this.host = configurationService.getRequired("org.breakout.api.host")
    }

    override fun getUserFromCustomUserDetails(customUserDetails: CustomUserDetails): User {
        val user = userRepository.findOne(customUserDetails.id) ?: throw Exception("User could be authenticated but data was not found")
        return user
    }

    override fun getUserById(id: Long): User? = userRepository.findOne(id)

    override fun getUserByActivationToken(token: String): User? = userRepository.findByActivationToken(token)

    override fun getUserByEmail(email: String): User? = userRepository.findByEmail(email)

    override fun getAllUsers(): Iterable<UserAccount> = userRepository.findAll()

    override fun exists(id: Long) = userRepository.exists(id)

    override fun exists(email: String) = userRepository.existsByEmail(email)

    override fun create(email: String, password: String): User {
        if (this.exists(email)) throw ConflictException("user with email $email already exists")
        val user = User.create(email, password)
        val token = user.createActivationToken()

        sendActivationEmail(token, user)

        return userRepository.save(user.account)
    }

    override fun activate(user: User, token: String) {
        if (user.isActivated()) throw DomainException("User already is activated")
        else if (!user.isActivationTokenCorrect(token)) throw DomainException("Incorrect activation token")
        else user.activate(token)
        this.save(user)
    }

    private fun sendActivationEmail(token: String, user: User) {
        mailService.sendUserHasRegisteredEmail(token, user)
    }

    override fun save(user: User): User = userRepository.save(user.account)

    override fun create(email: String, password: String, f: User.() -> Unit): User {
        val user = this.create(email, password)
        f.invoke(user)
        return this.save(user)
    }

    override fun requestReset(emailString: String) {
        val user = this.getUserByEmail(emailString) ?: throw NotFoundException("No user found with email")
        val token = user.createActivationToken()
        this.save(user)

        mailService.userWantsPasswordReset(user, token)
    }

    override fun resetPassword(emailString: String, password: String, token: String) {
        val user = this.getUserByEmail(emailString) ?: throw NotFoundException("No user found with email")
        user.setNewPassword(password, token)
        this.save(user)
    }

    override fun searchByString(search: String): List<UserAccount> {
        return userRepository.searchByString(search)
    }

    override fun findAllSponsors(): Iterable<Sponsor> {
        return userRepository.findAllSponsors()
    }
}

