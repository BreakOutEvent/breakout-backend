package backend.controller

import backend.controller.RequestBodies.Post
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@RestController
@RequestMapping("/test/post")
class PostController {

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createNewPost(@Valid @RequestBody post: Post): Any {
        return "{\"id\":\"1\"}"
    }
}
