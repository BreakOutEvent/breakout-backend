package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.misc.Coords
import backend.model.posting.Media
import backend.model.posting.MediaService
import backend.model.posting.MediaSizeService
import backend.model.posting.PostingService
import backend.view.MediaSizeView
import backend.view.PostingRequestView
import backend.view.PostingResponseView
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid
import javax.xml.bind.DatatypeConverter

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

        var location: Coords? = null
        if (body.postingLocation != null && body.postingLocation!!.latitude != null && body.postingLocation!!.longitude != null)
            location = Coords(body.postingLocation!!.latitude!!, body.postingLocation!!.longitude!!)

        var posting = postingService.createPosting(text = body.text, postingLocation = location, user = user.core!!, media = null)

        var media: MutableList<Media>? = null
        if (body.media != null && body.media!! is List<*>) {
            media = arrayListOf()
            body.media!!.forEach {
                media!!.add(Media(posting, it))
            }
        }

        posting.media = media

        postingService.save(posting)

        if (posting.media != null) {
            posting.media!!.forEach {
                it.uploadToken = Jwts.builder().setSubject(it.id.toString()).signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact();
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
            Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(JWT_SECRET)).parseClaimsJws(uploadToken).body.subject.equals(id.toString())
        } catch (e: MalformedJwtException) {
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
