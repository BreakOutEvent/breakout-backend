package backend.model.media

import org.springframework.data.jpa.repository.JpaRepository

interface MediaRepository : JpaRepository<Media, Long> {
    fun findById(id: Long): Media
}
