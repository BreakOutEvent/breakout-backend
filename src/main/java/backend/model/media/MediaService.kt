package backend.model.media

import backend.model.posting.Posting

interface MediaService {
    fun createMedia(type: String): Media
    fun findAll(): Iterable<Media>
    fun getByID(id: Long): Media?
    fun save(media: Media): Media?

}
