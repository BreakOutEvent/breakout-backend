package backend.services

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations

@Service
@Profile("test")
class FakeMailServiceImpl @Autowired constructor(restTemplate: RestOperations, configurationService: ConfigurationService, emailRepository: EmailRepository) : MailService by MailServiceImpl(restTemplate, configurationService, emailRepository) {

    val logger = LoggerFactory.getLogger(FakeMailServiceImpl::class.java)

    override fun send(email: Email, saveToDb: Boolean) {
        logger.info("Email to ${email.to} with subject \"${email.subject}\" would be sent now")
    }
}
