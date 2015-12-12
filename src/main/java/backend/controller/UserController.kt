package backend.controller

import backend.controller.RequestBodies.PostUserBody
import backend.model.user.UserCore
import backend.model.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@RestController
@RequestMapping("/test/user")
class UserController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addUser(@Valid @RequestBody body: PostUserBody): String {
        val user = UserCore()
        user.email = body.email!!
        user.firstname = body.firstname!!
        user.lastname = body.lastname!!
        user.gender = body.gender!!
        user.password = body.password!!
        userRepository.save(user.core)

        // TODO: Discuss what shall be returned here!
        return "{\"id\":\"1\"}"
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showUsers(): Iterable<UserCore> {
        return userRepository.findAll()
    }

//    @ExceptionHandler(Exception::class)
//    fun handle(e: Exception) {
//        e.printStackTrace()
//    }

}
