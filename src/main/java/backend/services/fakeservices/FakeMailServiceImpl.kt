package backend.services.fakeservices

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.services.ConfigurationService
import backend.services.MailService
import backend.services.MailServiceImpl
import backend.util.Profiles.DEVELOPMENT
import backend.util.Profiles.STAGING
import backend.util.Profiles.TEST
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@Service
@Profile(DEVELOPMENT, TEST, STAGING)
class FakeMailServiceImpl @Autowired constructor(restTemplate: RestOperations, configurationService: ConfigurationService, emailRepository: EmailRepository) : MailService by MailServiceImpl(restTemplate, configurationService, emailRepository) {

    private val logger = LoggerFactory.getLogger(FakeMailServiceImpl::class.java)
    private val pool = Executors.newCachedThreadPool()

    override fun send(email: Email, saveToDb: Boolean) {
        logger.info("Email to ${email.to} with subject \"${email.subject}\" and body \"${email.body}\" would be sent now")
        if (email.buttonUrl != null) logger.info("Email Button ${email.buttonUrl}")
    }

    override fun sendAsync(email: Email, saveToDb: Boolean) {
        pool.submit(Callable {
            send(email, saveToDb)
        })
    }
}
