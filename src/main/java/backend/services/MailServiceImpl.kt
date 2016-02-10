package backend.services

import backend.model.misc.Email
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class MailServiceImpl() : MailService {

    @Value("\${org.breakout.mailer.url}")
    private lateinit var url: String

    @Value("\${org.breakout.mailer.port}")
    private lateinit var port: String

    @Value("\${org.breakout.mailer.xauthtoken}")
    private lateinit var token: String

    var restTemplate: RestOperations = RestTemplate()

//    @Autowired
    constructor(restOperations: RestTemplate) : this() {
        this.restTemplate = restOperations
    }

    override fun send(email: Email) {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
            set("X-AUTH-TOKEN", "$token")
        }
        val body = ObjectMapper().writeValueAsString(email)
        println(body)
        val request = HttpEntity<String>(body, headers)
        restTemplate.exchange(getSendUrl(url, port), HttpMethod.POST, request, String::class.java)
    }

    private fun getSendUrl(baseUrl: String, port: String) = UriComponentsBuilder
            .fromHttpUrl("http://$baseUrl")
            .port(port)
            .path("send")
            .build().toUriString()
}
