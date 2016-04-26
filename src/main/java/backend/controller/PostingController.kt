package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.media.Media
import backend.model.media.MediaService
import backend.model.misc.Coord
import backend.model.posting.PostingService
import backend.model.user.Participant
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.distanceCoordsKM
import backend.util.getSignedJwtToken
import backend.util.toLocalDateTime
import backend.view.PostingView
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

@RestController
@RequestMapping("/posting")
open class PostingController {

    private val mediaService: MediaService
    private val postingService: PostingService
    private val configurationService: ConfigurationService
    private val logger: Logger
    private var JWT_SECRET: String
    private val userService: UserService

    @Autowired
    constructor(postingService: PostingService,
                mediaService: MediaService,
                configurationService: ConfigurationService,
                userService: UserService) {

        this.postingService = postingService
        this.mediaService = mediaService
        this.configurationService = configurationService
        this.logger = Logger.getLogger(PostingController::class.java)
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        this.userService = userService
    }


    /**
     * POST /posting/
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createPosting(@Valid @RequestBody body: PostingView,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): PostingView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)

        //check if any of the optional posting types is available
        if (body.uploadMediaTypes == null && body.text == null && body.postingLocation == null)
            throw BadRequestException("empty postings not allowed")

        var location: Coord? = null
        var distance: Double? = null

        val locationIsAvailable: Boolean = body.postingLocation != null
        if (locationIsAvailable) {
            location = Coord(body.postingLocation!!.latitude, body.postingLocation!!.longitude)
            val creator = user.getRole(Participant::class) ?: throw UnauthorizedException("User is no participant")
            val team = creator.currentTeam ?: throw UnauthorizedException("User has no team")

            //Calculate Distance from starting point of Event to Location Position and
            distance = distanceCoordsKM(team.event.startingLocation, location)
        }

        val date = body.date!!.toLocalDateTime()
        var posting = postingService.createPosting(text = body.text, postingLocation = location, user = user.core, mediaTypes = body.uploadMediaTypes, distance = distance, date = date)

        //Adds uploadingTokens to response
        posting.media?.forEach { it.uploadToken = getSignedJwtToken(JWT_SECRET, it.id.toString()) }

        return PostingView(posting)
    }

    /**
     * GET /posting/id/
     */
    @RequestMapping("/{id}/")
    open fun getPosting(@PathVariable("id") id: Long): PostingView {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        return PostingView(posting)
    }

    /**
     * GET /posting/
     */
    @RequestMapping("/")
    open fun getAllPostings(): Iterable<PostingView> {
        return postingService.findAll().map { PostingView(it) }
    }

    /**
     * POST /posting/get/ids/
     */
    @RequestMapping("/get/ids", method = arrayOf(POST))
    open fun getPostingsById(@Valid @RequestBody body: List<Long>): Iterable<PostingView> {
        return postingService.findAllByIds(body).map { PostingView(it) }
    }

    /**
     * GET /posting/get/since/id/
     */
    @RequestMapping("/get/since/{id}/")
    open fun getPostingIdsSince(@PathVariable("id") id: Long): Iterable<Long> {
        return postingService.findAllSince(id).map { it.id!! }
    }
}
