package backend.services

import backend.model.messaging.Message
import backend.model.user.UserAccount
import backend.util.Profiles.PRODUCTION
import backend.util.Profiles.STAGING
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
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
@Primary
@Profile(PRODUCTION, STAGING)
class NotificationServiceImpl(private val restTemplate: RestOperations,
                              configurationService: ConfigurationService) : NotificationService {

    private var url: String = configurationService.getRequired("org.breakout.api.notifications.url")
    private var apiKey: String = configurationService.getRequired("org.breakout.api.notifications.apiKey")
    private var appId: String = configurationService.getRequired("org.breakout.api.notifications.appId")
    private val pool = Executors.newCachedThreadPool()
    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)

    override fun notifyNewMessage(message: Message, groupId: Long?, users: List<UserAccount>) {

        val headers = HttpHeaders().apply {
            set("Authorization", "Basic $apiKey")
            set("Content-Type", "application/json;charset=utf-8")
        }

        val tokens = users.mapNotNull { it.notificationToken }

        val body = mapOf(
                "app_id" to appId,
                "data" to mapOf(
                        "id" to groupId
                ),
                "headings" to mapOf(
                        "en" to "New Message",
                        "de" to "Neue Nachricht"
                ),
                "contents" to mapOf(
                        "en" to message.text
                ),
                "include_player_ids" to tokens
        )

        val payload = ObjectMapper().writeValueAsString(body)

        val request = HttpEntity<String>(payload, headers)

        try {
            val sendUrl = getSendUrl(url)
            pool.submit(Callable {
                restTemplate.exchange(sendUrl, HttpMethod.POST, request, String::class.java)
            })
        } catch (e: Exception) {
            logger.error("""Error pushing notification "$message.text" to clients $tokens: ${e.message}""")
        }
    }

    private fun getSendUrl(baseUrl: String) = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("/api")
            .path("/v1")
            .path("/notifications")
            .build().toUriString()

}
