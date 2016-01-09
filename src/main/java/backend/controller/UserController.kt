package backend.controller

import backend.CustomUserDetails
import backend.controller.RequestBodies.PostUserBody
import backend.controller.ViewModels.UserViewModel
import backend.model.user.User
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import kotlin.collections.map
import kotlin.collections.mapOf

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
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = "/{id}/",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
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

    private fun User.apply(userViewModel: UserViewModel): User {

        this.firstname = userViewModel.firstname ?: this.firstname
        this.firstname = userViewModel.firstname ?: this.firstname
        this.lastname = userViewModel.lastname ?: this.lastname
        this.gender = userViewModel.gender ?: this.gender
        this.isBlocked = userViewModel.isBlocked ?: this.isBlocked

        if (userViewModel.participant != null) {

            if (!this.hasRole(backend.model.user.Participant::class.java)) {
                this.addRole(backend.model.user.Participant::class.java)
            }

            val p = this.getRole(backend.model.user.Participant::class.java) as backend.model.user.Participant
            p.apply {
                tshirtsize = userViewModel.participant?.tshirtsize ?: tshirtsize
                emergencynumber = userViewModel.participant?.emergencynumber ?: emergencynumber
                hometown = userViewModel.participant?.hometown ?: hometown
                phonenumber = userViewModel.participant?.phonenumber ?: phonenumber
            }
        }

        return this
    }

}
