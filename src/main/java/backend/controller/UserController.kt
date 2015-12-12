package backend.controller

import backend.controller.RequestBodies.PostUserBody
import backend.model.user.UserCore
import backend.model.user.UserRepository
import backend.model.user.UserService
import com.fasterxml.jackson.databind.util.JSONWrappedObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@RestController
@RequestMapping("/test/user")
class UserController {

    @Autowired
    private lateinit var userService: UserService

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addUser(@Valid @RequestBody body: PostUserBody): ResponseEntity<kotlin.Any> {
        try {
            var user = userService.create(body);
            // TODO: Discuss what shall be returned here!
            return ResponseEntity("{\"id\":\"1\"}", HttpStatus.CREATED)
        } catch (e: Exception) {
            // TODO: This catches all exceptions but should only catch those thrown if email already exists!
            return ResponseEntity(errorResponse("user already exists"), HttpStatus.BAD_REQUEST)
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showUsers(): MutableIterable<UserCore>? {
        return userService.getAllUsers();
    }

//    @ExceptionHandler(Exception::class)
//    fun handle(e: Exception) {
//        e.printStackTrace()
//    }

    data class errorResponse(var error: String)

}