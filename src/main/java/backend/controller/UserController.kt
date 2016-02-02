package backend.controller

import backend.configuration.CustomUserDetails
import backend.model.user.User
import backend.model.user.UserService
import backend.view.UserView
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

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
    @ApiOperation(value = "Create a new user", response = UserView::class)
    fun addUser(@Valid @RequestBody body: UserView): ResponseEntity<kotlin.Any> {

        if (userService.exists(body.email!!)) {
            return ResponseEntity(GeneralController.error("user with email ${body.email!!} already exists"), HttpStatus.CONFLICT)
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
    fun showUsers(): Iterable<UserView> {
        return userService.getAllUsers()!!.map { UserView(it) };
    }

    /**
     * PUT /user/id/
     */
    @RequestMapping(
            value = "/{id}/",
            method = arrayOf(RequestMethod.PUT),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun updateUser(@PathVariable("id") id: Long,
                   @Valid @RequestBody body: UserView,
                   @AuthenticationPrincipal user: CustomUserDetails): ResponseEntity<kotlin.Any> {

        if (user.core!!.id != id) {
            return ResponseEntity(GeneralController.error("authenticated user and requested resource mismatch"), HttpStatus.UNAUTHORIZED)
        }

        user.apply(body)
        userService.save(user)

        return ResponseEntity(UserView(user), HttpStatus.OK)
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

        if (user == null) return ResponseEntity(GeneralController.error("user with id $id does not exist"), HttpStatus.NOT_FOUND)
        else return ResponseEntity.ok(UserView(user))
    }


    //    @ExceptionHandler(Exception::class)
    //    fun handle(e: Exception) {
    //        e.printStackTrace()
    //    }


    private fun User.apply(userView: UserView): User {

        this.firstname = userView.firstname ?: this.firstname
        this.lastname = userView.lastname ?: this.lastname
        this.gender = userView.gender ?: this.gender

        if (userView.participant != null) {

            if (!this.hasRole(backend.model.user.Participant::class.java)) {
                this.addRole(backend.model.user.Participant::class.java)
            }

            val p = this.getRole(backend.model.user.Participant::class.java) as backend.model.user.Participant
            p.tshirtsize = userView.participant?.tshirtsize ?: p.tshirtsize
            p.emergencynumber = userView.participant?.emergencynumber ?: p.emergencynumber
            p.hometown = userView.participant?.hometown ?: p.hometown
            p.phonenumber = userView.participant?.phonenumber ?: p.phonenumber
        }

        return this
    }
}
