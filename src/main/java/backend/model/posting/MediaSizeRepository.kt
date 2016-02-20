package backend.model.posting

import org.springframework.data.repository.CrudRepository

interface MediaSizeRepository : CrudRepository<MediaSize, Long> {
    fun findById(id: Long): MediaSize
}
