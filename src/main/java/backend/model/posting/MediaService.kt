package backend.model.posting

interface MediaService {
    fun createMedia(posting: Posting, type: String): Media
    fun findAll(): Iterable<Media>
    fun getByID(id: Long): Media?
    fun save(media: Media): Media?

}
