package backend.controller;

import backend.controller.RequestBodies.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test/post")
public class PostController {


    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Object createNewPost(@Valid @RequestBody Post post) {
        return "{\"id\":\"1\"}";
    }
}
