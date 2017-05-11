package backend.model.posting

import backend.model.misc.Coord
import backend.model.user.User
import backend.model.user.UserAccount
import java.time.LocalDateTime

interface PostingService {

    fun savePostingWithLocationAndMedia(text: String?, postingLocation: Coord?, user: UserAccount, mediaTypes: List<String>?, date: LocalDateTime): Posting

    fun createPosting(user: User, text: String?, uploadMediaTypes: List<String>?, locationCoord: Coord?, clientDate: LocalDateTime): Posting

    fun findAll(page: Int, size: Int): List<Posting>

    fun findByEventIds(events: List<Long>, page: Int, size: Int): List<Posting>

    fun getByID(id: Long): Posting?

    fun save(posting: Posting): Posting?

    fun findByHashtag(hashtag: String, page: Int, size: Int): List<Posting>

    fun delete(posting: Posting)

    fun like(posting: Posting, account: UserAccount, timeCreated: LocalDateTime): Like

    fun unlike(by: UserAccount, from: Posting)

    fun removeComment(from: Posting, id: Long)

    fun addComment(to: Posting, from: UserAccount, at: LocalDateTime, withText: String): Comment
}
