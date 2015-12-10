package backend.controller;

import backend.controller.RequestBodies.PostUserBody;
import backend.model.user.User;
import backend.model.user.UserCore;
import backend.model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String addUser(@Valid @RequestBody PostUserBody body) {
        User user = new UserCore();
        user.setEmail(body.getEmail());
        user.setFirstname(body.getFirstname());
        user.setLastname(body.getLastname());
        user.setGender(body.getGender());
        user.setPassword(body.getPassword());
        userRepository.save(user.getCore());

        return "{\"id\":\"1\"}";
    }
}
