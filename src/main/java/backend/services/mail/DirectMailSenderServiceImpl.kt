package backend.services.mail

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.services.ConfigurationService
import backend.util.Profiles.DEVELOPMENT
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
import java.io.IOException
import com.sendgrid.*


@Service
@Profile(DEVELOPMENT)
class DirectMailSenderServiceImpl @Autowired constructor(private val emailRepository: EmailRepository,
                                                         configurationService: ConfigurationService) : MailSenderService {

    private val logger = LoggerFactory.getLogger(DirectMailSenderServiceImpl::class.java)
    private val pool = Executors.newCachedThreadPool()
    private val API_KEY = configurationService.get("org.breakout.sendgrid.apikey")

    override fun send(email: Email, saveToDb: Boolean) {
        val sg = SendGrid(API_KEY)
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = email.toSendgrid().build()
            val response = sg.api(request)
            println(response.statusCode)
            if (saveToDb) {
                email.isSent = true
                emailRepository.save(email)
            }
        } catch (ex: IOException) {
            if (saveToDb) {
                email.isSent = false
                emailRepository.save(email)
            }
            throw ex
        }

    }

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
