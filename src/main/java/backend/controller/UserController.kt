package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.TeamService
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.getSignedJwtToken
import backend.view.BasicUserView
import backend.view.DetailedInvitationView
import backend.view.UserView
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import java.time.LocalDate
import javax.validation.Valid

@Api
@RestController
@RequestMapping("/user")
open class UserController {

    private val userService: UserService
    private val JWT_SECRET: String
    private val configurationService: ConfigurationService
    private val teamService: TeamService

    @Autowired
    constructor(userService: UserService, teamService: TeamService, configurationService: ConfigurationService) {
        this.userService = userService
        this.configurationService = configurationService
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        this.teamService = teamService
    }

    /**
     * POST /user/
     * Registers a new user
     */
    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createUser(@Valid @RequestBody body: UserView): UserView {

        // Validate existence of email and password by hand
        // because UserView has those as optional because of PUT requests
        val email = body.email ?: throw BadRequestException("missing email")
        val password = body.password ?: throw BadRequestException("missing password")

        if (userService.exists(email)) throw ConflictException("email ${body.email!!} already exists")

        val user = userService.create(email, password)
        user.apply(body)
        userService.save(user)

        user.profilePic.uploadToken = getSignedJwtToken(JWT_SECRET, user.profilePic.id.toString())
        return UserView(user)
    }

    /**
     * POST /user/requestreset/
     * allows User to request password reset
     */
    @RequestMapping("/requestreset/", method = arrayOf(POST))
    open fun requestPasswordReset(@Valid @RequestBody body: Map<String, Any>): Map<String, String> {

        val emailString = body["email"] as? String ?: throw BadRequestException("body is missing field email")
        userService.requestReset(emailString)

        return mapOf("status" to "sent reset mail")
    }


    /**
     * POST /user/passwordreset/
     * Sets a new Password for User with given token
     */
    @RequestMapping("/passwordreset/", method = arrayOf(POST))
    open fun resetPassword(@Valid @RequestBody body: Map<String, Any>): Map<String, String> {

        val emailString = body["email"] as? String ?: throw BadRequestException("body is missing field email")
        val password = body["password"] as? String ?: throw BadRequestException("body is missing field password")
        val token = body["token"] as? String ?: throw BadRequestException("body is missing field token")

        userService.resetPassword(emailString, password, token)

        return mapOf("status" to "reset password")

    }

    /**
     * GET /user/
     * Gets all users
     */
    @RequestMapping("/", method = arrayOf(GET))
    open fun showUsers(): Iterable<BasicUserView> {
        return userService.getAllUsers()!!.map { BasicUserView(it) };
    }

    /**
     * PUT /user/{id}/
     * Edits user with given id
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/", method = arrayOf(PUT))
    open fun updateUser(@PathVariable id: Long,
                        @Valid @RequestBody body: UserView,
                        @AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.core.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")
        user.apply(body)
        userService.save(user)

        user.profilePic.uploadToken = getSignedJwtToken(JWT_SECRET, user.profilePic.id.toString())
        return UserView(user)
    }


    /**
     * GET /user/{id}/
     * Gets user with given id
     */
    @RequestMapping("/{id}/", method = arrayOf(GET))
    open fun showUser(@PathVariable id: Long): BasicUserView {

        val user = userService.getUserById(id) ?: throw NotFoundException("user with id $id does not exist")
        return BasicUserView(user)
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
        p.birthdate = try {
            LocalDate.parse(userView.participant?.birthdate)
        } catch (e: Exception) {
            p.birthdate
        }
        p.phonenumber = userView.participant?.phonenumber ?: p.phonenumber

        return this
    }

    /**
     * GET /user/invitation?token=lorem
     * Get an invitation including data such as email address via a token
     */
    @RequestMapping("/invitation", method = arrayOf(GET))
    open fun showInvitation(@RequestParam token: String): DetailedInvitationView {
        val invitation = teamService.findInvitationsByInviteCode(token) ?: throw NotFoundException("No invitation for code $token")
        return DetailedInvitationView(invitation)
    }
}
