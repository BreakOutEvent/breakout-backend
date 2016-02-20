package backend.controller

import backend.configuration.CustomUserDetails
import backend.model.misc.Coords
import backend.model.post.Media
import backend.model.post.MediaService
import backend.model.post.MediaSizeService
import backend.model.post.PostService
import backend.view.MediaSizeView
import backend.view.PostRequestView
import backend.view.PostResponseView
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
@RequestMapping("/post")
class PostController {

    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var mediaSizeService: MediaSizeService

    @Autowired
    private lateinit var mediaService: MediaService

    @Value("\${org.breakout.api.jwt_secret}")
    private lateinit var JWT_SECRET: String

    /**
     * Post /post/
     */
    @RequestMapping("/",method = arrayOf(RequestMethod.POST))
    fun createPost(@Valid @RequestBody body: PostRequestView,
                   @AuthenticationPrincipal user: CustomUserDetails): ResponseEntity<Any> {

        if (user.core!!.id == null) {
            return ResponseEntity(GeneralController.error("authenticated user and requested resource mismatch"), HttpStatus.UNAUTHORIZED)
        }

        if (body.media == null && body.text == null && body.postLocation == null) {
            return ResponseEntity(GeneralController.error("empty posts not allowed"), HttpStatus.BAD_REQUEST)
        }

        var location: Coords? = null
        if (body.postLocation != null && body.postLocation!!.latitude != null && body.postLocation!!.longitude != null)
            location = Coords(body.postLocation!!.latitude!!, body.postLocation!!.longitude!!)


        var post = postService.createPost(text = body.text, postLocation = location, user = user.core, media = null)

        var media: MutableList<Media>? = null
        if (body.media != null && body.media!! is List<*>) {
            media = arrayListOf()
            body.media!!.forEach {
                media!!.add(Media(post, it))
            }
        }

        post.media = media

        postService.save(post)

        if (post.media != null) {
            post.media!!.forEach {
                it.uploadToken = Jwts.builder().setSubject(it.id.toString()).signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact();
            }
        }

        return ResponseEntity(PostResponseView(post), HttpStatus.CREATED)
    }


    /**
     * POST /post/media/id/
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
     * GET /post/id/
     */
    @RequestMapping("/{id}/",method = arrayOf(GET))
    fun showPost(@PathVariable("id") id: Long): ResponseEntity<kotlin.Any> {

        val post = postService.getByID(id)

        if (post == null) {
            return ResponseEntity(GeneralController.error("post with id $id does not exist"), HttpStatus.NOT_FOUND)
        } else {
            return ResponseEntity.ok(PostResponseView(post))
        }
    }

    @RequestMapping("/", method = arrayOf(GET))
    fun getAllPosts(): Iterable<PostResponseView> {
        return postService.findAll().map { PostResponseView(it) }
    }

}
