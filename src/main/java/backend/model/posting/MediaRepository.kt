package backend.model.posting

import org.springframework.data.repository.CrudRepository

interface MediaRepository : CrudRepository<Media, Long> {
    fun findById(id: Long): Media
}
