package backend.services

import backend.model.misc.Email
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations

@Service
@Profile("test")
class FakeMailServiceImpl @Autowired constructor(restTemplate: RestOperations, configurationService: ConfigurationService) : MailService by MailServiceImpl(restTemplate, configurationService) {
    val logger = Logger.getLogger(FakeMailServiceImpl::class.java)

    override fun send(email: Email) {
        logger.info("Email to ${email.to} with subject \"${email.subject}\" would be sent now")
    }
}
