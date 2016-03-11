package backend.model.media

import org.springframework.data.repository.CrudRepository

interface MediaSizeRepository : CrudRepository<MediaSize, Long> {
    fun findById(id: Long): MediaSize
}
