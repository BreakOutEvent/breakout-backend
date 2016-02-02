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

@RestController
@RequestMapping("/post")
class PostController {

    @Autowired
    private lateinit var postService: PostService

    /**
     * Post /post/
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
        return ResponseEntity(PostView(post), HttpStatus.CREATED)

    }


    /**
     * GET /post/id/
     */
    @RequestMapping(
            value = "/{id}/",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showPost(@PathVariable("id") id: Long): ResponseEntity<kotlin.Any> {

        val post = postService.getByID(id)

        if (post == null) {
            return ResponseEntity(GeneralController.error("post with id $id does not exist"), HttpStatus.NOT_FOUND)
        } else {
            return ResponseEntity.ok(PostView(post))
        }
    }

}
