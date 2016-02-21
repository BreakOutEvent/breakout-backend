package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.exceptions.DomainException
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.view.UserView
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
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

    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    @ApiOperation(value = "Create a new user", response = UserView::class)
    fun addUser(@Valid @RequestBody body: UserView): UserView {

        if (userService.exists(body.email!!)) throw ConflictException("email ${body.email!!} already exists")

        val user = userService.create(body.email!!, body.password!!).apply(body)
        return UserView(userService.save(user)!!)
    }

    /**
     * GET /user/
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/", method = arrayOf(GET))
    fun showUsers(): Iterable<UserView> {
        return userService.getAllUsers()!!.map { UserView(it) };
    }

    /**
     * PUT /user/id/
     */
    @RequestMapping("/{id}/", method = arrayOf(PUT))
    fun updateUser(@PathVariable("id") id: Long,
                   @Valid @RequestBody body: UserView,
                   @AuthenticationPrincipal user: CustomUserDetails): UserView {

        if (user.core!!.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")
        user.apply(body)
        userService.save(user)
        return UserView(user)
    }


    /**
     * GET /user/id/
     */
    @RequestMapping("/{id}/", method = arrayOf(GET))
    fun showUser(@PathVariable("id") id: Long): UserView {

        val user = userService.getUserById(id)
        if (user == null) throw NotFoundException("user with id $id does not exist")
        return (UserView(user))
    }

    private fun User.apply(userView: UserView): User {

        this.firstname = userView.firstname ?: this.firstname
        this.lastname = userView.lastname ?: this.lastname
        this.gender = userView.gender ?: this.gender

        if (userView.participant == null) return this;

        if (!this.hasRole(Participant::class)) this.addRole(Participant::class)
        val p = this.getRole(Participant::class)!!
        p.tshirtsize = userView.participant?.tshirtsize ?: p.tshirtsize
        p.emergencynumber = userView.participant?.emergencynumber ?: p.emergencynumber
        p.hometown = userView.participant?.hometown ?: p.hometown
        p.phonenumber = userView.participant?.phonenumber ?: p.phonenumber

        return this
    }
}
