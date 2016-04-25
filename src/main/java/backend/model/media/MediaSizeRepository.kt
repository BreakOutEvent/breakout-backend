package backend.model.media

import org.springframework.data.repository.CrudRepository

interface MediaSizeRepository : CrudRepository<MediaSize, Long> {
    fun findById(id: Long): MediaSize?
    fun findByWidthAndMediaAndMediaType(width: Int, media: Media, type: MediaType): MediaSize?
    fun findByHeightAndMediaAndMediaType(height: Int, media: Media, type: MediaType): MediaSize?
}
