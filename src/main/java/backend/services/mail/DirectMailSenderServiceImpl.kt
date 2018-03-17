package backend.services.mail

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.services.ConfigurationService
import backend.util.Profiles.DEVELOPMENT
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executors


@Service
@Profile(DEVELOPMENT)
class DirectMailSenderServiceImpl @Autowired constructor(private val emailRepository: EmailRepository,
                                                         configurationService: ConfigurationService) : MailSenderService {

    private val logger = LoggerFactory.getLogger(DirectMailSenderServiceImpl::class.java)
    private val pool = Executors.newCachedThreadPool()

    override fun send(email: Email, saveToDb: Boolean) {
        if (saveToDb) {
            email.isSent = true
            emailRepository.save(email)
        }

    }

    override fun resendFailed(): Int {
        val failedMails = emailRepository.findByIsSent(false).take(100)
        failedMails.forEach { email ->
            send(email = email, saveToDb = true)
        }
        return failedMails.size
    }

    override fun sendAsync(email: Email, saveToDb: Boolean) {
        pool.submit(Callable {
            send(email, saveToDb)
        })
    }
}
