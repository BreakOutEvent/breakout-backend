package backend.model.user

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.exceptions.DomainException
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.services.ConfigurationService
import backend.services.MailService
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

    override fun getUserByEmail(email: String): User? = userRepository.findByEmail(email);

    override fun getAllUsers(): Iterable<UserCore> = userRepository.findAll()

    override fun exists(id: Long) = userRepository.exists(id)

    override fun exists(email: String) = userRepository.existsByEmail(email)

    override fun create(email: String, password: String): User {
        if (this.exists(email)) throw ConflictException("user with email $email already exists")
        val user = User.create(email, password)
        val token = user.createActivationToken()

        sendActivationEmail(token, user)

        return userRepository.save(user.core);
    }

    override fun activate(user: User, token: String) {
        if (user.isActivated()) throw DomainException("User already is activated")
        else if (!user.isActivationTokenCorrect(token)) throw DomainException("Incorrect activation token")
        else user.activate(token)
        this.save(user)
    }

    private fun sendActivationEmail(token: String, user: User) {
        val email = Email(
                to = listOf(EmailAddress(user.email)),
                subject = "BreakOut 2016 - Bitte aktiviere Deinen Account",
                body = "Vielen Dank für Dein Interesse an BreakOut 2016.<br><br>" +
                        "Zum Schutz Deiner Daten müssen wir sicherstellen, dass diese E-Mail-Adresse Dir gehört. Bitte klicke dazu auf den Button am Ende der E-Mail.<br><br>" +
                        "Du hast Dich nicht für BreakOut angemeldet? Dann ignoriere diese E-Mail einfach.<br><br>" +
                        "Du hast Fragen oder benötigst Unterstützung? Schreib uns eine E-Mail an <a href='mailto:event@break-out.org'>event@break-out.org</a>.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "E-MAIL-ADRESSE BESTÄTIGEN",
                buttonUrl = createActivationUrl(token),
                campaignCode = "confirm"
        )

        mailService.send(email)
    }

    private fun createActivationUrl(token: String): String {
        return "$host/activation/$token?utm_source=backend&utm_medium=email&utm_campaign=confirm"
    }

    private fun createResetUrl(token: String, email: String): String {
        return "$host/reset/$email/$token?utm_source=backend&utm_medium=email&utm_campaign=pwreset"
    }

    override fun save(user: User): User = userRepository.save(user.core)

    override fun create(email: String, password: String, f: User.() -> Unit): User {
        val user = this.create(email, password)
        f.invoke(user)
        return this.save(user)
    }

    override fun requestReset(emailString: String) {
        val user = this.getUserByEmail(emailString) ?: throw NotFoundException("No user found with email")
        val token = user.createActivationToken()
        this.save(user)

        val email = Email(
                to = listOf(EmailAddress(user.email)),
                subject = "BreakOut 2016 - Passwort zurücksetzen",
                body = "Hallo ${user.firstname ?: ""},<br><br>" +
                        "du hast angefordert dein Passwort für BreakOut zurück zu setzen.<br>" +
                        "Folge dem Knopf am Ende der Email um ein neues Passwort zu setzen.<br><br>" +
                        "Wenn du diese Email nicht angefordert hast, ignoriere sie einfach.<br><br>" +
                        "Liebe Grüße<br>" +
                        "Euer BreakOut-Team",
                buttonText = "PASSWORT ZURÜCKSETZEN",
                buttonUrl = createResetUrl(token, user.email),
                campaignCode = "pwreset"
        )

        mailService.send(email)
    }

    override fun resetPassword(emailString: String, password: String, token: String) {
        val user = this.getUserByEmail(emailString) ?: throw NotFoundException("No user found with email")
        user.setNewPassword(password, token)
        this.save(user)
    }

    override fun searchByString(search: String): List<UserCore> {
        return userRepository.searchByString(search)
    }
}

