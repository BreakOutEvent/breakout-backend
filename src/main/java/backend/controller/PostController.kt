package backend.controller

import backend.CustomUserDetails
import backend.model.event.PostService
import backend.model.misc.Coords
import backend.view.PostView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

import javax.validation.Valid
import kotlin.collections.mapOf

@RestController
@RequestMapping("/posting")
class PostController {

    @Autowired
    private lateinit var postService: PostService

    /**
     * Post /posting/
     */
    @RequestMapping(
            value = "/",
            method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createPost(@Valid @RequestBody body: PostView,
                   @AuthenticationPrincipal user: CustomUserDetails): ResponseEntity<Any> {

        if (user.core!!.id == null) {
            return ResponseEntity(GeneralController.error("authenticated user and requested resource mismatch"), HttpStatus.UNAUTHORIZED)
        }

        var post = postService.createPost(text = body.text!!, postLocation = Coords(body.postLocation!!.latitude!!, body.postLocation!!.longitude!!), user = user)
        postService.save(post)
        return ResponseEntity(mapOf("id" to post.id!!), HttpStatus.CREATED)

    }

}
