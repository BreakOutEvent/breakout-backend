package backend.services.mail

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.services.ConfigurationService
import backend.util.Profiles.PRODUCTION
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@Service
@Profile(PRODUCTION)
class MailSenderServiceImpl @Autowired constructor(private val restTemplate: RestOperations,
                                                   private val emailRepository: EmailRepository,
                                                   configurationService: ConfigurationService) : MailSenderService {

    private val url: String = configurationService.getRequired("org.breakout.mailer.url")
    private val token: String = configurationService.getRequired("org.breakout.mailer.xauthtoken")
    private val host: String = configurationService.getRequired("org.breakout.api.host")

    private val logger = LoggerFactory.getLogger(MailSenderServiceImpl::class.java)
    private val pool = Executors.newCachedThreadPool()

    override fun send(email: Email, saveToDb: Boolean) {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
            set("X-AUTH-TOKEN", token)
        }
        val body = ObjectMapper().writeValueAsString(email)
        val request = HttpEntity<String>(body, headers)

        logger.info("Content of email: $body")

        try {
            val sendurl = getSendUrl(url)
            logger.info("sending mail via: $sendurl")
            restTemplate.exchange(sendurl, HttpMethod.POST, request, String::class.java)
            if (saveToDb) {
                email.isSent = true
                emailRepository.save(email)
            }
        } catch (e: Exception) {
            logger.error(e.message)

            try {
                email.isSent = false
                emailRepository.save(email)
                logger.error("Can't send email via mailer. Saving to database. Cause: ${e.message}")
            } catch (e: Exception) {
                logger.error("Mailer rejected email and could also not be saved ${e.message}")
                logger.error("Content of failed email TO ${email.toAsString()} BCC ${email.bccAsString()} BODY:${email.body}")
            }


        }
    }

    private fun getSendUrl(baseUrl: String) = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("send")
            .build().toUriString()

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
