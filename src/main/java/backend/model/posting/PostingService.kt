package backend.model.posting

import backend.model.misc.Coord
import backend.model.user.User
import backend.model.user.UserCore
import java.time.LocalDateTime

interface PostingService {

    fun savePostingWithLocationAndMedia(text: String?, postingLocation: Coord?, user: UserCore, mediaTypes: List<String>?, date: LocalDateTime): Posting

    fun createPosting(user: User, text: String?, uploadMediaTypes: List<String>?, locationCoord: Coord?, clientDate: LocalDateTime): Posting

    fun findAll(offset: Int, limit: Int): List<Posting>

    fun findAll(): List<Posting>

    fun getByID(id: Long): Posting?

    fun save(posting: Posting): Posting?

    fun findAllByIds(body: List<Long>): Iterable<Posting>

    fun findAllIdsSince(id: Long): List<Long>

    fun findByHashtag(hashtag: String): List<Posting>

    fun delete(posting: Posting)

    fun like(posting: Posting, core: UserCore, timeCreated: LocalDateTime): Like

    fun unlike(by: UserCore, from: Posting)

    fun removeComment(from: Posting, id: Long)

    fun addComment(to: Posting, from: UserCore, at: LocalDateTime, withText: String): Comment
}
