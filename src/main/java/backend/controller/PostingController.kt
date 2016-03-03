package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.misc.Coord
import backend.model.posting.Media
import backend.model.posting.MediaService
import backend.model.posting.MediaSizeService
import backend.model.posting.PostingService
import backend.model.user.Participant
import backend.utils.distanceCoordsKM
import backend.view.MediaSizeView
import backend.view.PostingRequestView
import backend.view.PostingResponseView
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
import com.auth0.jwt.JWTVerifier
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import java.security.SignatureException
import javax.validation.Valid

@RestController
@RequestMapping("/posting")
class PostingController {

    @Autowired
    private lateinit var postingService: PostingService

    @Autowired
    private lateinit var mediaSizeService: MediaSizeService

    @Autowired
    private lateinit var mediaService: MediaService

    val logger = Logger.getLogger(PostingController::class.java)

    //    @Value("\${org.breakout.api.jwt_secret}")
    // TODO: Workaround for testing purposes
    private var JWT_SECRET: String = System.getenv("RECODER_JWT_SECRET") ?: "testsecret"

    /**
     * Post /posting/
     */
    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    fun createPosting(@Valid @RequestBody body: PostingRequestView,
                      @AuthenticationPrincipal user: CustomUserDetails): PostingResponseView {

        if (body.media == null && body.text == null && body.postingLocation == null)
            throw BadRequestException("empty postings not allowed")

        var location: Coord? = null
        var distance: Double? = null

        if (body.postingLocation != null && body.postingLocation!!.latitude != null && body.postingLocation!!.longitude != null) {
            location = Coord(body.postingLocation!!.latitude!!, body.postingLocation!!.longitude!!)
            val creator = user.getRole(Participant::class) ?: throw UnauthorizedException("User is no participant")
            val team = creator.currentTeam ?: throw UnauthorizedException("User has no team")

            //Calculate Distance from starting point of Event to Location Position and
            distance = distanceCoordsKM(team.event.startingLocation, location)
        }

        var posting = postingService.createPosting(text = body.text, postingLocation = location, user = user.core!!, media = null, distance = distance)

        //Create Media-Objects for each media item requested to add
        var media: MutableList<Media>? = null
        if (body.media != null && body.media!! is List<*>) {
            media = arrayListOf()
            body.media!!.forEach {
                media!!.add(Media(posting, it))
            }
        }
        posting.media = media

        postingService.save(posting)

        //Adds uploadingTokens to response
        if (posting.media != null) {
            posting.media!!.forEach {
                it.uploadToken = JWTSigner(JWT_SECRET).sign(mapOf("subject" to it.id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512))
            }
        }

        return PostingResponseView(posting)
    }


    /**
     * POST /posting/media/id/
     */
    @RequestMapping("/media/{id}/", method = arrayOf(RequestMethod.POST))
    @ResponseStatus(CREATED)
    fun createMediaSize(@PathVariable("id") id: Long,
                        @RequestHeader("X-UPLOAD-TOKEN") uploadToken: String,
                        @Valid @RequestBody body: MediaSizeView): MediaSizeView {

        try {
            if (!(JWTVerifier(JWT_SECRET, "audience").verify(uploadToken)["subject"] as String).equals(id.toString())) {
                throw UnauthorizedException("Invalid JWT token")
            }
        } catch (e: SignatureException) {
            throw UnauthorizedException(e.message ?: "Invalid JWT token")
        } catch (e: IllegalStateException) {
            throw UnauthorizedException(e.message ?: "Invalid JWT token")
        }

        val media = mediaService.getByID(id);
        var mediaSize = mediaSizeService.createMediaSize(media!!, body.url!!, body.width!!, body.height!!, body.length!!, body.size!!, body.type!!)

        mediaSizeService.save(mediaSize)
        return MediaSizeView(mediaSize)
    }

    /**
     * GET /posting/id/
     */
    @RequestMapping("/{id}/", method = arrayOf(GET))
    fun getPosting(@PathVariable("id") id: Long): PostingResponseView {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        return PostingResponseView(posting)
    }

    /**
     * GET /posting/
     */
    @RequestMapping("/", method = arrayOf(GET))
    fun getAllPostings(): Iterable<PostingResponseView> {
        return postingService.findAll().map { PostingResponseView(it) }
    }

    /**
     * POST /posting/get/ids/
     */
    @RequestMapping("/get/ids", method = arrayOf(POST))
    fun getPostingsById(@Valid @RequestBody body: List<Long>): Iterable<PostingResponseView> {
        return postingService.findAllByIds(body).map { PostingResponseView(it) }
    }

    /**
     * GET /posting/get/since/id/
     */
    @RequestMapping("/get/since/{id}/")
    fun getPostingIdsSince(@PathVariable("id") id: Long): Iterable<Long> {
        return postingService.findAllSince(id).map { it.id!! }
    }
}
