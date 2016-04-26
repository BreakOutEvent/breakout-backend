package backend.services

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder

@Service
@Profile("!test")
class MailServiceImpl : MailService {

    private val url: String
    private val token: String
    private val restTemplate: RestOperations
    private val emailRepository: EmailRepository

    private val logger = Logger.getLogger(MailServiceImpl::class.java)

    @Autowired
    constructor(restTemplate: RestOperations, configurationService: ConfigurationService, emailRepository: EmailRepository) {
        this.restTemplate = restTemplate
        this.emailRepository = emailRepository
        this.token = configurationService.getRequired("org.breakout.mailer.xauthtoken")
        this.url = configurationService.getRequired("org.breakout.mailer.url")
    }

    override fun send(email: Email, saveToDb: Boolean) {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
            set("X-AUTH-TOKEN", "$token")
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
            email.isSent = false
            emailRepository.save(email)
            logger.error("Mailer not available at this time, saved mail")
        }
    }

    private fun getSendUrl(baseUrl: String) = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("send")
            .build().toUriString()

    override fun resendFailed(): Int {
        val failedMails = emailRepository.findByIsSent(false)
        failedMails.forEach { email ->
            send(email = email, saveToDb = true)
        }
        return failedMails.size
    }
}