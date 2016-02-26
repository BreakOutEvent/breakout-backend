package backend.model.posting

import backend.model.misc.Coord
import backend.model.user.UserCore

interface PostingService {
    fun createPosting(text: String?, postingLocation: Coord?, user: UserCore, media: MutableList<Media>?): Posting
    fun findAll(): Iterable<Posting>
    fun getByID(id: Long): Posting?
    fun save(posting: Posting): Posting?
    fun findAllByIds(body: List<Long>): Iterable<Posting>
    fun findAllSince(id: Long): Iterable<Posting>
}
