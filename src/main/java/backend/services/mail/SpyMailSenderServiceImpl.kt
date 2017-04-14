package backend.services.mail

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.util.Profiles.DEVELOPMENT
import backend.util.Profiles.STAGING
import backend.util.Profiles.TEST
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@Service
@Profile(DEVELOPMENT, TEST, STAGING)
open class SpyMailSenderServiceImpl @Autowired constructor(private val emailRepository: EmailRepository) : MailSenderService {

    private val logger = LoggerFactory.getLogger(SpyMailSenderServiceImpl::class.java)
    private val pool = Executors.newCachedThreadPool()

    override fun send(email: Email, saveToDb: Boolean) {
        logger.info("Email to ${email.to} with subject \"${email.subject}\" and body \"${email.body}\" would be sent now")
        if (email.buttonUrl != null) logger.info("Email Button ${email.buttonUrl}")
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

