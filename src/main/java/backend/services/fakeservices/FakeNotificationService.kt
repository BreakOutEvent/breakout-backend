package backend.services.fakeservices

import backend.model.challenges.Challenge
import backend.model.messaging.Message
import backend.model.posting.Comment
import backend.model.posting.Like
import backend.model.posting.Posting
import backend.model.user.UserAccount
import backend.services.NotificationService
import backend.util.Profiles.DEVELOPMENT
import backend.util.Profiles.TEST
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(DEVELOPMENT, TEST)
class FakeNotificationService : NotificationService {

    val logger: Logger = LoggerFactory.getLogger(FakeNotificationService::class.java)

    override fun notifyNewMessage(message: Message, groupId: Long?, users: List<UserAccount>) {
        logger.info("""Fakely sending "$message.text" in group $groupId""")
    }

    override fun notifyNewChallenge(challenge: Challenge, users: List<UserAccount>) {
        logger.info("Fakely notifying about new challenge ${challenge.description}")
    }

    override fun notifyNewComment(comment: Comment, posting: Posting, users: List<UserAccount>) {
        logger.info("Fakely notifying about new comment ${comment.text} on posting ${posting.id}")
    }

    override fun notifyNewLike(like: Like, posting: Posting, users: List<UserAccount>) {
        logger.info("Fakely notifying about new like in ${posting.id}")
    }

    override fun notifyChallengeCompleted(challenge: Challenge, posting: Posting) {
        logger.info("Fakely notifying about completed challenge ${challenge.description} in ${posting.id}")
    }

}
