package backend.model.posting

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MediaSizeServiceImpl @Autowired constructor(val repository: MediaSizeRepository) : MediaSizeService {

    override fun save(mediaSize: MediaSize): MediaSize = repository.save(mediaSize)

    override fun findAll(): Iterable<MediaSize> = repository.findAll()

    override fun createMediaSize(media: Media, url: String, width: Int, height: Int, length: Int, size: Long, type: String): MediaSize {
        val mediaSize = MediaSize(media, url, width, height, length, size, type)
        return repository.save(mediaSize)
    }

    override fun getByID(id: Long): MediaSize? {
        return repository.findById(id)
    }
}
