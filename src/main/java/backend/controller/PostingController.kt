package backend.controller

import backend.configuration.CustomUserDetails
import backend.model.misc.Coords
import backend.model.posting.Media
import backend.model.posting.MediaService
import backend.model.posting.MediaSizeService
import backend.model.posting.PostingService
import backend.view.MediaSizeView
import backend.view.PostingRequestView
import backend.view.PostingResponseView
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.GET
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

    @Value("\${org.breakout.api.jwt_secret}")
    private lateinit var JWT_SECRET: String

    /**
     * Post /posting/
     */
    @RequestMapping("/",method = arrayOf(RequestMethod.POST))
    fun createPost(@Valid @RequestBody body: PostingRequestView,
                   @AuthenticationPrincipal user: CustomUserDetails): ResponseEntity<Any> {

        if (user.core!!.id == null) {
            return ResponseEntity(GeneralController.error("authenticated user and requested resource mismatch"), HttpStatus.UNAUTHORIZED)
        }

        if (body.media == null && body.text == null && body.postingLocation == null) {
            return ResponseEntity(GeneralController.error("empty postings not allowed"), HttpStatus.BAD_REQUEST)
        }

        var location: Coords? = null
        if (body.postingLocation != null && body.postingLocation!!.latitude != null && body.postingLocation!!.longitude != null)
            location = Coords(body.postingLocation!!.latitude!!, body.postingLocation!!.longitude!!)


        var posting = postingService.createPosting(text = body.text, postingLocation = location, user = user.core, media = null)

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

        return ResponseEntity(PostingResponseView(posting), HttpStatus.CREATED)
    }


    /**
     * POST /posting/media/id/
     */
    @RequestMapping("/media/{id}/",method = arrayOf(RequestMethod.POST))
    fun createMediaSize(@PathVariable("id") id: Long,
                        @RequestHeader("X-UPLOAD-TOKEN") uploadToken: String,
                        @Valid @RequestBody body: MediaSizeView): ResponseEntity<Any> {

        try {
            var isTokenValid = Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(uploadToken).body.subject.equals(id.toString());
            if (isTokenValid) {

                val media = mediaService.getByID(id);
                var mediaSize = mediaSizeService.createMediaSize(media!!, body.url!!, body.width!!, body.height!!, body.length!!, body.size!!, body.type!!)

                mediaSizeService.save(mediaSize)
                return ResponseEntity(MediaSizeView(mediaSize), HttpStatus.CREATED)
            } else {
                return ResponseEntity(GeneralController.error("authenticated user and requested resource mismatch"), HttpStatus.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            return ResponseEntity(GeneralController.error("authenticated user and requested resource mismatch"), HttpStatus.UNAUTHORIZED)
        }
    }

    /**
     * GET /posting/id/
     */
    @RequestMapping("/{id}/",method = arrayOf(GET))
    fun getPosting(@PathVariable("id") id: Long): ResponseEntity<kotlin.Any> {

        val posting = postingService.getByID(id)

        if (posting == null) {
            return ResponseEntity(GeneralController.error("posting with id $id does not exist"), HttpStatus.NOT_FOUND)
        } else {
            return ResponseEntity.ok(PostingResponseView(posting))
        }
    }

    @RequestMapping("/", method = arrayOf(GET))
    fun getAllPosts(): Iterable<PostingResponseView> {
        return postingService.findAll().map { PostingResponseView(it) }
    }

}
