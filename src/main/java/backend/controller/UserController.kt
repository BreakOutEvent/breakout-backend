package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.view.UserView
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
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

    private val userService: UserService
    private val JWT_SECRET: String
    private val configurationService: ConfigurationService

    @Autowired
    constructor(userService: UserService, configurationService: ConfigurationService) {
        this.userService = userService
        this.configurationService = configurationService
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
    }

    /**
     * POST /user/
     */

    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    @ApiOperation(value = "Create a new user", response = UserView::class)
    fun addUser(@Valid @RequestBody body: UserView): UserView {

        // Validate existence of email and password by hand
        // because UserView has those as optional because of PUT requests
        val email = body.email ?: throw BadRequestException("missing email")
        val password = body.password ?: throw BadRequestException("missing password")

        if (userService.exists(email)) throw ConflictException("email ${body.email!!} already exists")

        val user = userService.create(email, password).apply(body)

        user.profilePic.uploadToken = JWTSigner(JWT_SECRET).sign(mapOf("subject" to user.profilePic.id.toString()), JWTSigner.Options().setAlgorithm(Algorithm.HS512))

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
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.core.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")
        user.apply(body)
        userService.save(user)
        return UserView(user)
    }


    /**
     * GET /user/id/
     */
    @RequestMapping("/{id}/", method = arrayOf(GET))
    fun showUser(@PathVariable("id") id: Long): UserView {

        val user = userService.getUserById(id) ?: throw NotFoundException("user with id $id does not exist")
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
