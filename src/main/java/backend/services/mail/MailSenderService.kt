package backend.services.mail

import backend.model.misc.Email
import backend.model.misc.EmailRepository
import backend.services.ConfigurationService
import backend.util.Profiles
import backend.util.Profiles.DEVELOPMENT
import backend.util.Profiles.PRODUCTION
import backend.util.Profiles.STAGING
import backend.util.Profiles.TEST
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

interface MailSenderService {
    fun send(email: Email, saveToDb: Boolean = false)

    fun resendFailed(): Int

    fun sendAsync(email: Email, saveToDb: Boolean = false)
}

