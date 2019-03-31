package backend.services

import backend.model.messaging.Message
import backend.model.user.UserAccount
import backend.model.posting.Posting
import backend.model.posting.Comment
import backend.model.posting.Like
import backend.model.challenges.Challenge

interface NotificationService {
    fun notifyNewMessage(message: Message, groupId: Long?, users: List<UserAccount>)
    fun notifyNewChallenge(challenge: Challenge, users: List<UserAccount>)
    fun notifyNewComment(comment: Comment, posting: Posting, users: List<UserAccount>)
    fun notifyNewLike(like: Like, posting: Posting, users: List<UserAccount>)
    fun notifyChallengeCompleted(challenge: Challenge, posting: Posting)
}