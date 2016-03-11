package backend.model.media

import backend.model.posting.Posting
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MediaServiceImpl @Autowired constructor(val repository: MediaRepository) : MediaService {

    override fun save(media: Media): Media = repository.save(media)

    override fun findAll(): Iterable<Media> = repository.findAll()

    override fun createMedia(posting: Posting, type: String): Media {
        val media = Media(posting, type)
        return repository.save(media)
    }

    override fun getByID(id: Long): Media? {
        return repository.findById(id)
    }
}
