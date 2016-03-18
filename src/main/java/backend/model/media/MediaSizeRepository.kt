package backend.model.media

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface MediaSizeRepository : CrudRepository<MediaSize, Long> {
    fun findById(id: Long): MediaSize

    @Modifying
    @Query("DELETE FROM MediaSize s WHERE s.width = :width AND s.media.id = :mediaId")
    fun deleteByWidthAndMediaId(@Param("width") width: Int, @Param("mediaId") mediaId: Long)

    @Modifying
    @Query("DELETE FROM MediaSize s WHERE s.height = :height AND s.media.id = :mediaId")
    fun deleteByHeightAndMediaId(@Param("height") height: Int, @Param("mediaId") mediaId: Long)

    fun findByWidthAndMedia(width: Int, media: Media): MediaSize
    fun findByHeightAndMedia(height: Int, media: Media): MediaSize
}
