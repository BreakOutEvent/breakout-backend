package backend.model.media

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MediaServiceImpl @Autowired constructor(val repository: MediaRepository) : MediaService {

    override fun save(media: Media): Media = repository.save(media)

    override fun findAll(): Iterable<Media> = repository.findAll()

    override fun createMedia(type: String, url: String): Media {
        val media = Media(MediaType.valueOf(type), url)
        return repository.save(media)
    }

    override fun getByID(id: Long): Media? {
        return repository.findById(id)
    }

    override fun delete(media: Media) = repository.delete(media)

}
