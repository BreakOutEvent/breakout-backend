package backend.services

import backend.configuration.SimpleCORSFilter
import backend.model.messaging.GroupMessage
import backend.model.misc.EmailRepository
import backend.model.user.UserAccount
import backend.view.NotificationView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import org.slf4j.LoggerFactory

@Service
class NotificationServiceImpl @Autowired constructor(private val restTemplate: RestOperations,
                                                     private var configurationService: ConfigurationService): NotificationService {

    private var url: String = configurationService.getRequired("org.breakout.api.notifications.url")
    private var appId: String = configurationService.getRequired("org.breakout.api.notifications.appId")
    private val pool = Executors.newCachedThreadPool()

    private val log = LoggerFactory.getLogger(SimpleCORSFilter::class.java)

    override fun send(title: String, subtitle: String?, data: GroupMessage, users: List<UserAccount>) {

        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json;charset=utf-8")
        }

        val tokens = users.mapNotNull { it.notificationToken }
                            .map { "\"" + it + "\"" }
        val joined: String
        if (tokens.isEmpty()) {
            joined = ""
        } else {
            joined = tokens.reduce { acc, s -> acc + ", " + s }
        }

        val body = "{ \"app_id\": \"" + appId + "\", \"headings\": { \"en\": \"" + title + "\"}, \"contents\": { \"en\": \"" + subtitle + "\"}, \"include_player_ids\": [" + joined + "] }"

        val request = HttpEntity<String>(body, headers)

        try {
            val sendurl = getSendUrl(url)
            pool.submit(Callable {
                restTemplate.exchange(sendurl, HttpMethod.POST, request, String::class.java)
            })
        } catch (e: Exception) {
            // TODO: handle errors
        }
    }

    private fun getSendUrl(baseUrl: String) = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("/api")
            .path("/v1")
            .path("/notifications")
            .build().toUriString()

}
