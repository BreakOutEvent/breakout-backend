package backend.services

import backend.model.challenges.Challenge
import backend.model.messaging.Message
import backend.model.posting.Comment
import backend.model.posting.Like
import backend.model.posting.Posting
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

    private data class Translations(val german: String, val english: String = german)

    override fun notifyNewMessage(message: Message, groupId: Long?, users: List<UserAccount>) {
        val senderName = message.creator.fullName()
        val tokens = users.mapNotNull { it.notificationToken }
        send(
                mapOf("id" to groupId),
                Translations("$senderName hat dir eine Nachricht geschickt", "$senderName sent you a message"),
                Translations(message.text),
                tokens
        )
    }

    override fun notifyNewChallenge(challenge: Challenge, users: List<UserAccount>) {
        val sponsorName = "${challenge.sponsor.firstname} ${challenge.sponsor.lastname}"
        val challengeText = "${challenge.description} (${challenge.amount})"
        val tokens = users.mapNotNull { it.notificationToken }
        send(
                mapOf("challengeId" to challenge.id),
                Translations("Neue Challenge von $sponsorName", "New Challenge from $sponsorName"),
                Translations(challengeText),
                tokens
        )
    }

    override fun notifyChallengeCompleted(challenge: Challenge, posting: Posting) {
        val tokens = arrayOf(challenge.sponsor.registeredSponsor?.account).mapNotNull { it?.notificationToken }
        send(
                mapOf("postingId" to posting.id),
                Translations("Team ${challenge.team?.name} hat deine Challenge erfüllt", "Team ${challenge.team?.name} completed your challenge"),
                Translations(challenge.description),
                tokens
        )
    }

    override fun notifyNewComment(comment: Comment, posting: Posting, users: List<UserAccount>) {
        val commenterName = comment.user?.fullName()
        val tokens = users.mapNotNull { it.notificationToken }
        send(
                mapOf("postingId" to posting.id),
                Translations("$commenterName hat auf dein Post kommentiert", "$commenterName commented on your Post"),
                Translations(comment.text),
                tokens
        )
    }

    override fun notifyNewLike(like: Like, posting: Posting, users: List<UserAccount>) {
        val likerName = like.user?.fullName()
        val tokens = users.mapNotNull { it.notificationToken }
        send(
                mapOf("postingId" to posting.id),
                Translations("$likerName hat dein Post geliked", "$likerName liked your Post"),
                null,
                tokens
        )
    }

    private fun send(data: Map<String, *>,
                     headings: Translations,
                     contents: Translations?,
                     tokens: List<String>) {

        if (tokens.isEmpty()) return

        val headers = HttpHeaders().apply {
            set("Authorization", "Basic $apiKey")
            set("Content-Type", "application/json;charset=utf-8")
        }

        val body = mapOf(
                "app_id" to appId,
                "data" to data,
                "headings" to headings.map(),
                "contents" to contents?.map(),
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
            logger.error("""Error pushing notification to clients $tokens: ${e.message}""")
        }
    }

    private fun getSendUrl(baseUrl: String) = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("/api")
            .path("/v1")
            .path("/notifications")
            .build().toUriString()

    private fun Translations.map(): Map<String, String> = mapOf(
            "de" to german,
            "en" to english
    )

    private fun UserAccount.fullName(): String = "$firstname $lastname"

}
