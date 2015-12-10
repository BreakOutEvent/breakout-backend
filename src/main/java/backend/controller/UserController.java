package backend.controller;

import backend.controller.RequestBodies.PostUserBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test/user")
public class UserController {

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String addUser(@Valid @RequestBody PostUserBody body) {
        return "{\"id\":\"1\"}";
    }
}
