package backend.services

import backend.Integration.toJsonString
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.misc.URL
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate

class MailServiceImplTest {

    private val BASE_URL = "localhost"
    private val PORT = "3000"

    private lateinit var restTemplate: RestTemplate
    private lateinit var mailService: MailService
    private lateinit var mockServer: MockRestServiceServer

    @Before
    fun setUp() {
        restTemplate = RestTemplate()
        val configurationService = Mockito.mock(ConfigurationService::class.java)
        Mockito.`when`(configurationService.getRequired("org.breakout.mailer.xauthtoken")).thenReturn("randomtoken")
        Mockito.`when`(configurationService.getRequired("org.breakout.mailer.url")).thenReturn(BASE_URL)
        Mockito.`when`(configurationService.getRequired("org.breakout.mailer.port")).thenReturn(PORT)
        mailService = MailServiceImpl(restTemplate, configurationService)
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    @Test
    fun testSend() {

        val response = mapOf("success" to "ok", "mailerId" to "somerandomid").toJsonString()

        mockServer.expect(requestTo("http://$BASE_URL:$PORT/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header("Content-Type", "application/json;charset=utf-8"))
                .andExpect(MockRestRequestMatchers.header("X-AUTH-TOKEN", "randomtoken"))
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").isArray)
                .andExpect(MockRestRequestMatchers.jsonPath("$.subject").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.html").exists())
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        val email = createEmail()
        mailService.send(email)
        mockServer.verify()
    }

    @Test
    fun testSendWithAttachments() {
        val response = mapOf("success" to "ok", "mailerId" to "somerandomid").toJsonString()

        mockServer.expect(requestTo("http://$BASE_URL:$PORT/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header("Content-Type", "application/json;charset=utf-8"))
                .andExpect(MockRestRequestMatchers.header("X-AUTH-TOKEN", "randomtoken"))
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").isArray)
                .andExpect(MockRestRequestMatchers.jsonPath("$.subject").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.html").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.files").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.files").isArray)
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        val email = createEmailWithAttachements()
        mailService.send(email)
        mockServer.verify()
    }

    @Test
    fun testWithBccs() {
        val response = mapOf("success" to "ok", "mailerId" to "somerandomid").toJsonString()

        mockServer.expect(requestTo("http://$BASE_URL:$PORT/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header("Content-Type", "application/json;charset=utf-8"))
                .andExpect(MockRestRequestMatchers.header("X-AUTH-TOKEN", "randomtoken"))
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").isArray)
                .andExpect(MockRestRequestMatchers.jsonPath("$.subject").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.html").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.bccs").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.bccs").isArray)
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        val email = createEmailWithBCCs()
        mailService.send(email)
        mockServer.verify()
    }

    @Test
    fun testSendWithCampaignCode() {
        val response = mapOf("success" to "ok", "mailerId" to "somerandomid").toJsonString()

        mockServer.expect(requestTo("http://$BASE_URL:$PORT/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header("Content-Type", "application/json;charset=utf-8"))
                .andExpect(MockRestRequestMatchers.header("X-AUTH-TOKEN", "randomtoken"))
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.tos").isArray)
                .andExpect(MockRestRequestMatchers.jsonPath("$.subject").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.html").exists())
                .andExpect(MockRestRequestMatchers.jsonPath("$.campaign_code").exists())
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        val email = createEmailWithCampaignCode()
        mailService.send(email)
        mockServer.verify()
    }

    private fun createEmail(): Email {
        return Email(
                to = listOf(EmailAddress("florian.schmidt.1994@icloud.com"), EmailAddress("florian.schmidt.1994@gmail.com")),
                subject = "An awesome email",
                body = "<html>Awesome html email</html>")
    }

    private fun createEmailWithAttachements(): Email {
        val basicEmail = createEmail()
        return Email(
                to = basicEmail.to,
                subject = basicEmail.subject,
                body = basicEmail.body,
                files = listOf(URL("http://www.google.de/somelink"), URL("http://www.google.de/somelink"))
        )
    }

    private fun createEmailWithBCCs(): Email {
        val basicEmail = createEmail()
        return Email(
                to = basicEmail.to,
                subject = basicEmail.subject,
                body = basicEmail.body,
                bcc = listOf(EmailAddress("bcc@mail.de"), EmailAddress("bcc2@mail.de"))
        )
    }

    private fun createEmailWithCampaignCode(): Email {
        val basicEmail = createEmail()
        return Email(
                to = basicEmail.to,
                subject = basicEmail.subject,
                body = basicEmail.body,
                campaignCode = "campaignCode123456"
        )
    }
}
