package backend.model.post

interface MediaSizeService {
    fun createMediaSize(media: Media, url: String, width: Int, height: Int, length: Float): MediaSize
    fun findAll(): Iterable<MediaSize>
    fun getByID(id: Long): MediaSize?
    fun save(mediaSize: MediaSize): MediaSize?

}
