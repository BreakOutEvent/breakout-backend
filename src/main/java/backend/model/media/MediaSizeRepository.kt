package backend.model.media

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface MediaSizeRepository : CrudRepository<MediaSize, Long> {
    fun findById(id: Long): MediaSize?

    @Query("from MediaSize ms where ms.width between (:width - 5) and (:width + 5) and ms.media = :media and ms.mediaType = :type")
    fun findByWidthAndMediaAndMediaType(@Param("width") width: Int, @Param("media") media: Media, @Param("type") type: MediaType): MediaSize?

    @Query("from MediaSize ms where ms.height between (:height - 5) and (:height + 5) and ms.media = :media and ms.mediaType = :type")
    fun findByHeightAndMediaAndMediaType(@Param("height") height: Int, @Param("media") media: Media, @Param("type") type: MediaType): MediaSize?

    @Modifying
    @Query("delete from MediaSize ms where ms.media.id = :mediaId and (unix_timestamp(now()) - unix_timestamp(created_at)) > 60")
    fun deleteOlderOneMinute(@Param("mediaId") mediaId: Long)

}
