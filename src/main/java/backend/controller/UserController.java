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

        // TODO: Discuss what shall be returned here!
        return "{\"id\":\"1\"}";
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<UserCore> showUsers() {
        return userRepository.findAll();
    }

//    @ExceptionHandler(Exception.class)
//    public void handle(Exception e) {
//        e.printStackTrace();
//    }

}
