package backend.services.fakeservices

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.services.ConfigurationService
import backend.services.MailService
import backend.services.MailServiceImpl
import backend.util.Profiles.DEVELOPMENT
import backend.util.Profiles.TEST
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations

@Service
@Profile(DEVELOPMENT, TEST)
class FakeMailServiceImpl @Autowired constructor(restTemplate: RestOperations, configurationService: ConfigurationService, emailRepository: EmailRepository) : MailService by MailServiceImpl(restTemplate, configurationService, emailRepository) {

    val logger = LoggerFactory.getLogger(FakeMailServiceImpl::class.java)

    override fun send(email: Email, saveToDb: Boolean) {
        logger.info("Email to ${email.to} with subject \"${email.subject}\" would be sent now")
        logger.info("Email Body ${email.body}")
        if (email.buttonUrl != null) logger.info("Email Button ${email.buttonUrl}")
    }
}
