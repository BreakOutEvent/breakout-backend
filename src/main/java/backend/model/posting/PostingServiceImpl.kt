package backend.model.posting

import backend.controller.exceptions.BadRequestException
import backend.exceptions.DomainException
import backend.model.location.Location
import backend.model.location.LocationService
import backend.model.media.Media
import backend.model.media.MediaRepository
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PostingServiceImpl @Autowired constructor(val repository: PostingRepository,
                                                val locationService: LocationService,
                                                val mediaRepository: MediaRepository) : PostingService {

    override fun removeComment(posting: Posting, commentId: Long) {
        posting.removeComment(commentId)
        this.save(posting)
    }

    // TODO: This should return Posting (will break API)
    override fun addComment(posting: Posting, from: UserCore, at: LocalDateTime, withText: String): Comment {
        val comment = posting.addComment(from, at, withText)
        this.save(posting)
        return comment
    }

    // TODO: This should return Posting (will break API)
    override fun like(posting: Posting, core: UserCore, timeCreated: LocalDateTime): Like {
        val like = posting.like(timeCreated, core)
        this.save(posting)
        return like // TODO: Transactional?
    }

    override fun unlike(by: UserCore, from: Posting) {
        from.unlike(by)
        this.save(from)
    }

    override fun findAllIdsSince(id: Long): List<Long> = repository.findAllIdsSince(id)

    override fun findAllByIds(body: List<Long>): Iterable<Posting> = repository.findAllByIds(body)

    override fun save(posting: Posting): Posting = repository.save(posting)!!

    override fun findAll(offset: Int, limit: Int): List<Posting> = repository.findAllByOrderByIdDesc(PageRequest(offset, limit))

    override fun findAll(): List<Posting> = repository.findAllByOrderByIdDesc()

    @Transactional
    override fun savePostingWithLocationAndMedia(text: String?,
                                                 postingLocation: Coord?,
                                                 user: UserCore,
                                                 mediaTypes: List<String>?,
                                                 date: LocalDateTime): Posting {

        var location: Location? = null
        if (postingLocation != null) {
            val uploader = user.getRole(Participant::class) ?: throw DomainException("user is no participant and can therefor not upload location")
            location = locationService.create(postingLocation, uploader, date, true)
        }

        //Create Media-Objects for each media item requested to add
        val media: MutableList<Media> = arrayListOf()
        mediaTypes?.forEach {
            media.add(Media(it))
        }

        return repository.save(Posting(text, date, location, user, media))
    }

    override fun createPosting(user: User,
                               text: String?,
                               uploadMediaTypes: List<String>?,
                               locationCoord: Coord?,
                               clientDate: LocalDateTime): Posting {

        //check if any of the optional posting types is available
        if (uploadMediaTypes == null && (text == null || text.trim() == "") && locationCoord == null)
            throw BadRequestException("empty postings not allowed")

        return this.savePostingWithLocationAndMedia(text, locationCoord, user.core, uploadMediaTypes, clientDate)
    }

    override fun getByID(id: Long): Posting? = repository.findById(id)

    override fun findByHashtag(hashtag: String): List<Posting> = repository.findByHashtag(hashtag)

    @Transactional
    override fun delete(posting: Posting) {
        posting.media.forEach {
            mediaRepository.delete(it)
        }

        repository.delete(posting)
    }
}
