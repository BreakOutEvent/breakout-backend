package backend.model.posting

interface MediaSizeService {
    fun createAndSaveMediaSize(media: Media, url: String, width: Int, height: Int, length: Int, size: Long, type: String): MediaSize
    fun findAll(): Iterable<MediaSize>
    fun getByID(id: Long): MediaSize?
    fun save(mediaSize: MediaSize): MediaSize?

}
