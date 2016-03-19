package backend.model.media

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MediaSizeServiceImpl : MediaSizeService {


    private val repository: MediaSizeRepository
    private val mediaRepository: MediaRepository

    @Autowired
    constructor(mediaSizeRepository: MediaSizeRepository, mediaRepository: MediaRepository) {
        this.repository = mediaSizeRepository
        this.mediaRepository = mediaRepository
    }

    override fun save(mediaSize: MediaSize): MediaSize = repository.save(mediaSize)

    override fun findAll(): Iterable<MediaSize> = repository.findAll()

    override fun createAndSaveMediaSize(media: Media, url: String, width: Int, height: Int, length: Int, size: Long, type: String): MediaSize {
        val mediaSize = MediaSize(media, url, width, height, length, size, type)
        val mediaSizeSaved = this.save(mediaSize)
        media.sizes!!.add(mediaSizeSaved)
        mediaRepository.save(media)
        return mediaSizeSaved
    }

    override fun getByID(id: Long): MediaSize? {
        return repository.findById(id)
    }

    override fun findByWidthAndMediaAndMediaType(width: Int, media: Media, type: MediaType): MediaSize? {
        return repository.findByWidthAndMediaAndMediaType(width, media, type)
    }

    override fun findByHeightAndMediaAndMediaType(height: Int, media: Media, type: MediaType): MediaSize? {
        return repository.findByHeightAndMediaAndMediaType(height, media, type)
    }
}
