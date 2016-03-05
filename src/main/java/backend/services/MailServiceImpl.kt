package backend.services

import backend.model.misc.Email
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
    private val port: String
    private val token: String
    private val restTemplate: RestOperations

    private val logger = Logger.getLogger(MailServiceImpl::class.java)

    @Autowired
    constructor(restTemplate: RestOperations, configurationService: ConfigurationService) {
        this.restTemplate = restTemplate
        this.port = configurationService.getRequired("org.breakout.mailer.port")
        this.token = configurationService.getRequired("org.breakout.mailer.xauthtoken")
        this.url = configurationService.getRequired("org.breakout.mailer.url")
    }

    override fun send(email: Email) {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
            set("X-AUTH-TOKEN", "$token")
        }
        val body = ObjectMapper().writeValueAsString(email)
        val request = HttpEntity<String>(body, headers)
        try {
            val sendurl = getSendUrl(url, port)
            logger.info("sending mail via: $sendurl")
            restTemplate.exchange(sendurl, HttpMethod.POST, request, String::class.java)
        } catch (e: Exception) {
            logger.error(e.message)
            // TODO: Implement better fallback in this case!
            logger.error("Mailer not available at this time")
        }

    }

    private fun getSendUrl(baseUrl: String, port: String) = UriComponentsBuilder
            .fromHttpUrl("http://$baseUrl")
            .port(port)
            .path("send")
            .build().toUriString()
}
