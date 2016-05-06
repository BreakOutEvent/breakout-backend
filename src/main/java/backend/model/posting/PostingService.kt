package backend.model.posting

import backend.model.misc.Coord
import backend.model.user.User
import backend.model.user.UserCore
import backend.view.LocationView
import java.time.LocalDateTime

interface PostingService {

    fun savePostingWithLocationAndMedia(text: String?, postingLocation: Coord?, user: UserCore, mediaTypes: List<String>?, distance: Double?, date: LocalDateTime): Posting

    fun createPosting(user: User, text: String?, uploadMediaTypes: List<String>?, postingLocation: LocationView?, date: Long?): Posting

    fun findAll(): Iterable<Posting>

    fun getByID(id: Long): Posting?

    fun save(posting: Posting): Posting?

    fun findAllByIds(body: List<Long>): Iterable<Posting>

    fun findAllSince(id: Long): Iterable<Posting>

}
