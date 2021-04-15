package backend.model.user

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.model.media.Media
import backend.model.media.MediaService
import backend.services.ConfigurationService
import backend.services.mail.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class UserServiceImpl @Autowired constructor(private val userRepository: UserRepository,
                                             private val mailService: MailService,
                                             private val mediaService: MediaService,
                                             configurationService: ConfigurationService) : UserService {
    private val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    private val host: String = configurationService.getRequired("org.breakout.api.host")

    override fun getUserFromCustomUserDetails(customUserDetails: CustomUserDetails): User {
        return userRepository.findOne(customUserDetails.id) ?: throw Exception("User could be authenticated but data was not found")
    }

    override fun getUserById(id: Long): User? = userRepository.findOne(id)

    override fun getUserByActivationToken(token: String): User? = userRepository.findByActivationToken(token)

    override fun getUserByEmail(email: String): User? = userRepository.findByEmail(email)

    override fun getAllUsers(): Iterable<UserAccount> = userRepository.findAll()

    override fun getAllUsersBlockedBy(userId: Long): Iterable<UserAccount> = userRepository.findAllUsersBlockedByUser(userId)

    override fun getAllAdmins(): Iterable<UserAccount> {
        return userRepository.findAllUsersByRole(Admin::class.java)
    }

    override fun exists(id: Long) = userRepository.exists(id)

    override fun exists(email: String) = userRepository.existsByEmail(email)

    override fun create(email: String, password: String): User {
        if (this.exists(email)) throw ConflictException("user with email $email already exists")
        val user = User.create(email, password, false)
        val token = user.createActivationToken()

        sendActivationEmail(token, user)

        return userRepository.save(user.account)
    }

    override fun createN(email: String, password: String, newsletter: Boolean): User {
        if (this.exists(email)) throw ConflictException("user with email $email already exists")
        val user = User.create(email, password, newsletter)
        user.account.newsletter = newsletter
        logger.info(user.account.newsletter.toString())
        
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

    override fun save(user: User): User {

        if (user.profilePic != null) {
            user.profilePic = mediaService.save(user.profilePic as Media)
        }

        return userRepository.save(user.account)
    }

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

    override fun swapPasswords(first: UserAccount, second: UserAccount) {
        val tmp = first.passwordHash
        first.passwordHash = second.passwordHash
        second.passwordHash = tmp

        save(first)
        save(second)
    }
}

