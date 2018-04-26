package backend.model.posting

import backend.controller.exceptions.BadRequestException
import backend.exceptions.DomainException
import backend.model.location.Location
import backend.model.location.LocationService
import backend.model.media.Media
import backend.model.media.MediaService
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserAccount
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PostingServiceImpl(private val repository: PostingRepository,
                         private val locationService: LocationService,
                         private val mediaService: MediaService,
                         private val applicationEventPublisher: ApplicationEventPublisher) : PostingService {

    override fun removeComment(posting: Posting, commentId: Long) {
        posting.removeComment(commentId)
        this.save(posting)
    }

    // TODO: This should return Posting (will break API)
    override fun addComment(posting: Posting, from: UserAccount, at: LocalDateTime, withText: String): Comment {
        val comment = posting.addComment(from, at, withText)
        this.save(posting)
        return comment
    }

    // TODO: This should return Posting (will break API)
    override fun like(posting: Posting, account: UserAccount, timeCreated: LocalDateTime): Like {
        val like = posting.like(timeCreated, account)
        this.save(posting)
        return like // TODO: Transactional?
    }

    override fun unlike(by: UserAccount, from: Posting) {
        from.unlike(by)
        this.save(from)
    }

    override fun save(posting: Posting): Posting = repository.save(posting)!!

    override fun findAll(page: Int, size: Int): List<Posting> = repository.findAllByOrderByIdDesc(PageRequest(page, size))

    override fun findByEventIds(events: List<Long>, page: Int, size: Int): List<Posting> {
        return repository.findByTeamEventIdInOrderByIdDesc(events, PageRequest(page, size))
    }

    override fun findReported(): List<Posting> = repository.findReported()

    @Transactional
    override fun savePostingWithLocationAndMedia(text: String?,
                                                 postingLocation: Coord?,
                                                 user: UserAccount,
                                                 media: Media?,
                                                 date: LocalDateTime): Posting {

        var location: Location? = null
        if (postingLocation != null) {
            val uploader = user.getRole(Participant::class) ?: throw DomainException("user is no participant and can therefor not upload location")
            location = locationService.create(postingLocation, uploader, date, true)
        }

        var savedMedia: Media? = null
        if (media != null) {
            savedMedia = mediaService.save(media)
        }

        //applicationEventPublisher.publishEvent(PostingCreatedEvent(posting))
        return repository.save(Posting(text, date, location, user, savedMedia))
    }

    @Transactional
    override fun adminSavePostingWithLocationAndMedia(text: String?,
                                                      postingLocation: Coord?,
                                                      user: UserAccount,
                                                      media: Media?,
                                                      date: LocalDateTime): Posting {

        var location: Location? = null
        if (postingLocation != null) {
            val uploader = user.getRole(Participant::class) ?: throw DomainException("user is no participant and can therefor not upload location")
            location = locationService.adminCreate(postingLocation, uploader, date, true)
        }

        var savedMedia: Media? = null
        if (media != null) {
            savedMedia = mediaService.save(media)
        }

        return repository.save(Posting(text, date, location, user, savedMedia))
    }


    override fun createPosting(user: User,
                               text: String?,
                               media: Media?,
                               locationCoord: Coord?,
                               clientDate: LocalDateTime): Posting {

        //check if any of the optional posting types is available
        if (media == null && (text == null || text.trim() == "") && locationCoord == null)
            throw BadRequestException("empty postings not allowed")

        return this.savePostingWithLocationAndMedia(text, locationCoord, user.account, media, clientDate)
    }

    override fun adminCreatePosting(user: User,
                                    text: String?,
                                    media: Media?,
                                    locationCoord: Coord?,
                                    clientDate: LocalDateTime): Posting {

        //check if any of the optional posting types is available
        if (media == null && (text == null || text.trim() == "") && locationCoord == null)
            throw BadRequestException("empty postings not allowed")

        return this.adminSavePostingWithLocationAndMedia(text, locationCoord, user.account, media, clientDate)
    }

    override fun getByID(id: Long): Posting? = repository.findById(id)

    override fun findByHashtag(hashtag: String, page: Int, size: Int): List<Posting> = repository.findByHashtag(hashtag, PageRequest(page, size))

    @Transactional
    override fun delete(posting: Posting) {
        repository.delete(posting)
    }
}

class PostingCreatedEvent(val posting: Posting)
