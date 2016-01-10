package backend.controller

import backend.CustomUserDetails
import backend.controller.ViewModels.UserViewModel
import backend.model.user.User
import backend.model.user.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import kotlin.collections.map
import kotlin.collections.mapOf

@Api
@RestController
@RequestMapping("/user")
class UserController {

    @Autowired
    private lateinit var userService: UserService

    /**
     * POST /user/
     */

    @RequestMapping(
            value = "/",
            method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ApiOperation(value = "Create a new user", response = UserViewModel::class)
    fun addUser(@Valid @RequestBody body: UserViewModel): ResponseEntity<kotlin.Any> {

        if (userService.exists(body.email!!)) {
            return ResponseEntity(error("user with email ${body.email!!} already exists"), HttpStatus.CONFLICT)
        }

        var user = userService.create(body.email!!, body.password!!).apply(body)
        userService.save(user)
        return ResponseEntity(mapOf("id" to user.core!!.id!!), HttpStatus.CREATED)
    }

    /**
     * GET /user/
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = "/",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showUsers(): Iterable<UserViewModel> {
        return userService.getAllUsers()!!.map { UserViewModel(it) };
    }

    /**
     * PUT /user/id/
     */
    @RequestMapping(
            value = "/{id}/",
            method = arrayOf(RequestMethod.PUT),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun updateUser(@PathVariable("id") id: Long,
                   @Valid @RequestBody body: UserViewModel,
                   @AuthenticationPrincipal user: CustomUserDetails): ResponseEntity<kotlin.Any> {

        if (user.core!!.id != id) {
            return ResponseEntity(error("authenticated user and requested resource mismatch"), HttpStatus.UNAUTHORIZED)
        }

        user.apply(body)
        userService.save(user)

        return ResponseEntity(UserViewModel(user), HttpStatus.OK)
    }


    /**
     * GET /user/id/
     */
    @RequestMapping(
            value = "/{id}/",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showUser(@PathVariable("id") id: Long): ResponseEntity<kotlin.Any> {

        val user = userService.getUserById(id)

        if (user == null) return ResponseEntity(error("user with id $id does not exist"), HttpStatus.NOT_FOUND)
        else return ResponseEntity.ok(UserViewModel(user))
    }


    //    @ExceptionHandler(Exception::class)
    //    fun handle(e: Exception) {
    //        e.printStackTrace()
    //    }

    private data class error(var error: String)

    private fun User.apply(userViewModel: UserViewModel): User {

        this.firstname = userViewModel.firstname ?: this.firstname
        this.lastname = userViewModel.lastname ?: this.lastname
        this.gender = userViewModel.gender ?: this.gender

        if (userViewModel.participant != null) {

            if (!this.hasRole(backend.model.user.Participant::class.java)) {
                this.addRole(backend.model.user.Participant::class.java)
            }

            val p = this.getRole(backend.model.user.Participant::class.java) as backend.model.user.Participant
            p.tshirtsize = userViewModel.participant?.tshirtsize ?: p.tshirtsize
            p.emergencynumber = userViewModel.participant?.emergencynumber ?: p.emergencynumber
            p.hometown = userViewModel.participant?.hometown ?: p.hometown
            p.phonenumber = userViewModel.participant?.phonenumber ?: p.phonenumber
        }

        return this
    }
}
