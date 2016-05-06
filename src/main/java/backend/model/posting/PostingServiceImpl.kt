package backend.model.posting

import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.UnauthorizedException
import backend.exceptions.DomainException
import backend.model.location.Location
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserCore
import backend.util.distanceCoordsKM
import backend.util.toLocalDateTime
import backend.view.LocationView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PostingServiceImpl @Autowired constructor(val repository: PostingRepository) : PostingService {
    override fun findAllSince(id: Long): Iterable<Posting> = repository.findAllSince(id)

    override fun findAllByIds(body: List<Long>): Iterable<Posting> = repository.findAll(body)

    override fun save(posting: Posting): Posting = repository.save(posting)!!

    override fun findAll(): Iterable<Posting> = repository.findAll()

    override fun savePostingWithLocationAndMedia(text: String?,
                                                 postingLocation: Coord?,
                                                 user: UserCore,
                                                 mediaTypes: List<String>?,
                                                 distance: Double?,
                                                 date: LocalDateTime): Posting {

        var location: Location? = null
        if (postingLocation != null) {
            val uploader = user.getRole(Participant::class) ?: throw DomainException("user is no participant and can therefor not upload location")
            location = Location(postingLocation, uploader, date, distance)
        }

        //Create Media-Objects for each media item requested to add
        var media: MutableList<Media>?
        media = arrayListOf()
        mediaTypes?.forEach {
            media!!.add(Media(it))
        }

        return repository.save(Posting(text, date, location, user, media))
    }

    override fun createPosting(user: User,
                               text: String?,
                               uploadMediaTypes: List<String>?,
                               postingLocation: LocationView?,
                               date: Long?): Posting {

        //check if any of the optional posting types is available
        if (uploadMediaTypes == null && text == null && postingLocation == null)
            throw BadRequestException("empty postings not allowed")

        var location: Coord? = null
        var distance: Double? = null

        val locationIsAvailable: Boolean = postingLocation != null
        if (locationIsAvailable) {
            location = Coord(postingLocation!!.latitude, postingLocation.longitude)
            val creator = user.getRole(Participant::class) ?: throw UnauthorizedException("User is no participant")
            val team = creator.currentTeam ?: throw UnauthorizedException("User has no team")

            //Calculate Distance from starting point of Event to Location Position and
            distance = distanceCoordsKM(team.event.startingLocation, location)
        }

        val clientDate = date!!.toLocalDateTime()
        return this.savePostingWithLocationAndMedia(text = text, postingLocation = location, user = user.core, mediaTypes = uploadMediaTypes, distance = distance, date = clientDate)
    }

    override fun getByID(id: Long): Posting? = repository.findById(id)
}
