package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.TeamService
import backend.model.misc.Url
import backend.model.user.*
import backend.services.ConfigurationService
import backend.util.getSignedJwtToken
import backend.view.BasicUserView
import backend.view.DetailedInvitationView
import backend.view.SimpleUserView
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
        user.setValuesFrom(body)
        userService.save(user)

        // Dynamically generate upload tokens before showing view to user
        user.profilePic.generateSignedUploadToken(JWT_SECRET)
        user.getRole(Sponsor::class)?.logo?.generateSignedUploadToken(JWT_SECRET)

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
        return userService.getAllUsers().map { BasicUserView(it) };
    }


    /**
     * GET /user/search/{search}/
     * Searches for User by String greater 2 chars
     */
    @RequestMapping("/search/{search}/", method = arrayOf(GET))
    open fun getPostingsByHashtag(@PathVariable("search") search: String): List<SimpleUserView> {
        if (search.length < 3) return listOf()
        val users = userService.searchByString(search)
        return users.map { SimpleUserView(it) }
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

        user.setValuesFrom(body)
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

    private fun User.setValuesFrom(userView: UserView): User {

        this.firstname = userView.firstname ?: this.firstname
        this.lastname = userView.lastname ?: this.lastname
        this.gender = userView.gender ?: this.gender

        userView.participant?.let { this.becomeOrModifyParticipant(it) }
        userView.sponsor?.let { this.becomeOrModifySponsor(it) }

        return this
    }

    private fun User.becomeOrModifyParticipant(participantViewModel: UserView.ParticipantViewModel) {
        val p = this.getRole(Participant::class) ?: this.addRole(Participant::class)

        p.tshirtsize = participantViewModel.tshirtsize ?: p.tshirtsize
        p.emergencynumber = participantViewModel.emergencynumber ?: p.emergencynumber
        p.hometown = participantViewModel.hometown ?: p.hometown
        p.birthdate = try {
            LocalDate.parse(participantViewModel.birthdate)
        } catch (e: Exception) {
            p.birthdate
        }
        p.phonenumber = participantViewModel.phonenumber ?: p.phonenumber
    }

    private fun User.becomeOrModifySponsor(sponsorView: UserView.SponsorView): User {
        val sponsor: Sponsor = this.getRole(Sponsor::class) ?: this.addRole(Sponsor::class)

        sponsor.address = sponsorView.address?.toAddress() ?: sponsor.address
        sponsor.isHidden = sponsorView.isHidden ?: sponsor.isHidden
        sponsor.company = sponsorView.company ?: sponsor.company

        val urlString = sponsorView.url
        if (urlString != null) sponsor.url = Url(urlString)

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

fun UserView.AddressView.toAddress(): Address? {
    return Address(this.street!!, this.housenumber!!, this.city!!, this.country!!, this.zipcode!!)
}
