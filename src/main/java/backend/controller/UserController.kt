package backend.controller

import backend.controller.RequestBodies.PostUserBody
import backend.controller.RequestBodies.PutUserBody
import backend.model.user.UserCore
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import kotlin.collections.mapOf
import kotlin.text.toLong

@RestController
@RequestMapping("/user")
class UserController {

    @Autowired
    private lateinit var userService: UserService

    /**
     * POST /user/
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping("/", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addUser(@Valid @RequestBody body: PostUserBody): ResponseEntity<kotlin.Any> {

        if (userService.exists(body.email!!)) {
            return ResponseEntity(error("user with email ${body.email!!} already exists"), HttpStatus.BAD_REQUEST)
        }

        var user = userService.create(body);
        return ResponseEntity(mapOf("id" to user!!.core!!.id!!), HttpStatus.CREATED)
    }

    /**
     * GET /user/
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showUsers(): MutableIterable<UserCore>? {
        return userService.getAllUsers();
    }

    /**
     * PUT /user/id/
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/{id}/", method = arrayOf(RequestMethod.PUT), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun updateUser(@PathVariable("id") id: String, @Valid @RequestBody body: PutUserBody): ResponseEntity<kotlin.Any> {

        val user = userService.getUserById(id.toLong())

        if (user == null) {
            return ResponseEntity(error("user with id $id does not exist"), HttpStatus.BAD_REQUEST)
        }

        user.apply {
            firstname = body.firstname ?: user.firstname
            lastname = body.lastname ?: user.lastname
            gender = body.gender ?: user.gender
            isBlocked = body.isBlocked ?: user.isBlocked
        }

        userService.save(user)

        return ResponseEntity(user, HttpStatus.OK)
    }

    /**
     * GET /user/id/
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/{id}/", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showUser(@PathVariable("id") id: Long): ResponseEntity<kotlin.Any> {

        userService.getUserById(id)?.let {
            return ResponseEntity(it, HttpStatus.OK)
        }

        return ResponseEntity(error("user with id $id does not exist"), HttpStatus.NOT_FOUND)
    }


    //    @ExceptionHandler(Exception::class)
    //    fun handle(e: Exception) {
    //        e.printStackTrace()
    //    }

    private data class error(var error: String)

}