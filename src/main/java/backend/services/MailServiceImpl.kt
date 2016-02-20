package backend.services

import backend.model.misc.Email
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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

    @Value("\${org.breakout.mailer.url}")
    private lateinit var url: String

    @Value("\${org.breakout.mailer.port}")
    private lateinit var port: String

    @Value("\${org.breakout.mailer.xauthtoken}")
    private lateinit var token: String

    private lateinit var restTemplate: RestOperations

    private val logger = Logger.getLogger(MailServiceImpl::class.java)

    @Autowired
    constructor(restTemplate: RestOperations) {
        this.restTemplate = restTemplate
    }

    override fun send(email: Email) {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
            set("X-AUTH-TOKEN", "$token")
        }
        val body = ObjectMapper().writeValueAsString(email)
        val request = HttpEntity<String>(body, headers)
        try {
            restTemplate.exchange(getSendUrl(url, port), HttpMethod.POST, request, String::class.java)
        } catch (e: Exception) {
            logger.error("Mailer not available at this time")
        }

    }

    private fun getSendUrl(baseUrl: String, port: String) = UriComponentsBuilder
            .fromHttpUrl("http://$baseUrl")
            .port(port)
            .path("send")
            .build().toUriString()
}
