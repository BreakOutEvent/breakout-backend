package backend.model.post

import org.springframework.data.repository.CrudRepository

interface MediaRepository : CrudRepository<Media, Long> {
    fun findById(id: Long): Media
}
