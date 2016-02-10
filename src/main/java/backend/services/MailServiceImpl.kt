package backend.services

import backend.model.misc.Email
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
@Profile("!test")
class MailServiceImpl() : MailService {

    @Value("\${org.breakout.mailer.url}")
    private lateinit var url: String

    @Value("\${org.breakout.mailer.port}")
    private lateinit var port: String

    @Value("\${org.breakout.mailer.xauthtoken}")
    private lateinit var token: String

    // TODO: How to autowire this, failed on normal approach
    var restTemplate: RestOperations = RestTemplate()

    override fun send(email: Email) {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
            set("X-AUTH-TOKEN", "$token")
        }
        val body = ObjectMapper().writeValueAsString(email)
        val request = HttpEntity<String>(body, headers)
        restTemplate.exchange(getSendUrl(url, port), HttpMethod.POST, request, String::class.java)
    }

    private fun getSendUrl(baseUrl: String, port: String) = UriComponentsBuilder
            .fromHttpUrl("http://$baseUrl")
            .port(port)
            .path("send")
            .build().toUriString()
}

@Service
@Profile("test")
class FakeMailServiceImpl : MailService by MailServiceImpl() {

    val logger = Logger.getLogger(FakeMailServiceImpl::class.java)

    override fun send(email: Email) {
        logger.info("Email to ${email.to} with subject \"${email.subject}\" would be sent now")
    }
}
