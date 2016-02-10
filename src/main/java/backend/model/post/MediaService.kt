package backend.model.post

interface MediaService {
    fun createMedia(post: Post, type: String): Media
    fun findAll(): Iterable<Media>
    fun getByID(id: Long): Media?
    fun save(media: Media): Media?

}
