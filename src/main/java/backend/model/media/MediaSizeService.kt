package backend.model.media

interface MediaSizeService {
    fun createAndSaveMediaSize(mediaId: Long, url: String, width: Int, height: Int, length: Int, size: Long, type: String): MediaSize
    fun createOrUpdate(mediaId: Long, url: String, width: Int, height: Int, length: Int, size: Long, type: String): MediaSize
    fun findAll(): Iterable<MediaSize>
    fun getByID(id: Long): MediaSize?
    fun save(mediaSize: MediaSize): MediaSize?
    fun findByWidthAndMediaAndMediaType(width: Int, media: Media, type: MediaType): MediaSize?
    fun findByHeightAndMediaAndMediaType(height: Int, media: Media, type: MediaType): MediaSize?
}
