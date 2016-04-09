package backend.model.posting

import backend.exceptions.DomainException
import backend.model.location.Location
import backend.model.location.Point
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PostingServiceImpl @Autowired constructor(val repository: PostingRepository) : PostingService {
    override fun findAllSince(id: Long): Iterable<Posting> = repository.findAllSince(id)

    override fun findAllByIds(body: List<Long>): Iterable<Posting> = repository.findAll(body)

    override fun save(posting: Posting): Posting = repository.save(posting)!!

    override fun findAll(): Iterable<Posting> = repository.findAll()

    override fun createPosting(text: String?,
                               postingLocation: Coord?,
                               user: UserCore,
                               media: MutableList<Media>?,
                               distance: Double?,
                               date: LocalDateTime): Posting {

        var location: Location? = null
        if (postingLocation != null) {
            val uploader = user.getRole(Participant::class)
                    ?: throw DomainException("user is no participant and can therefor not upload location")
            location = Location(Point(postingLocation.latitude, postingLocation.longitude), uploader, date)
        }

        return repository.save(Posting(text, location, user, media, distance))
    }

    override fun getByID(id: Long): Posting? = repository.findById(id)
}
