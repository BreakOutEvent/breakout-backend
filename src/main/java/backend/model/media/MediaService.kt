package backend.model.media

interface MediaService {

    fun createMedia(type: String, url: String): Media

    fun findAll(): Iterable<Media>

    fun getByID(id: Long): Media?

    fun save(media: Media): Media?

    fun delete(media: Media)

}
