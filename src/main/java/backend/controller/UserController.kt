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
import backend.view.DetailedInvitationView
import backend.view.NotificationTokenView
import backend.view.user.BasicUserView
import backend.view.user.SimpleUserView
import backend.view.user.UserView
import io.swagger.annotations.Api
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.validation.Valid

@Api
@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService,
                     private val teamService: TeamService,
                     configurationService: ConfigurationService) {

    private val JWT_SECRET: String = configurationService.getRequired("org.breakout.api.jwt_secret")
    private val logger: Logger = LoggerFactory.getLogger(UserController::class.java)

    /**
     * POST /user/
     * Registers a new user
     */
    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun createUser(@Valid @RequestBody body: UserView): UserView {

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
    @PostMapping("/requestreset/")
    fun requestPasswordReset(@Valid @RequestBody body: Map<String, Any>): Map<String, String> {

        val emailString = body["email"] as? String ?: throw BadRequestException("body is missing field email")
        userService.requestReset(emailString)

        return mapOf("status" to "sent reset mail")
    }


    /**
     * POST /user/passwordreset/
     * Sets a new Password for User with given token
     */
    @PostMapping("/passwordreset/")
    fun resetPassword(@Valid @RequestBody body: Map<String, Any>): Map<String, String> {

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
    @GetMapping("/")
    fun showUsers(): Iterable<BasicUserView> {
        return userService.getAllUsers().map(::BasicUserView)
    }

    /**
     * GET /user/search/{search}/
     * Searches for User by String greater 2 chars
     */
    @GetMapping("/search/{search}/")
    fun searchUsers(@PathVariable("search") search: String): List<SimpleUserView> {
        if (search.length < 3) return listOf()
        val users = userService.searchByString(search).take(6).toMutableList()
        users.addAll(teamService.searchByString(search).take(3).flatMap { it.members.map { it.account } })
        return users.map(::SimpleUserView)
    }

    /**
     * PUT /user/{id}/
     * Edits user with given id
     */
    @CacheEvict("postings")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/")
    fun updateUser(@PathVariable id: Long,
                   @Valid @RequestBody body: UserView,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.account.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")

        user.setValuesFrom(body)
        userService.save(user)

        user.profilePic.uploadToken = getSignedJwtToken(JWT_SECRET, user.profilePic.id.toString())
        return UserView(user)
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/notificationtoken")
    fun updateNotificationToken(@PathVariable id: Long,
                                @Valid @RequestBody token: NotificationTokenView,
                                @AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.account.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")

        user.notificationToken = token.token
        userService.save(user)
        return UserView(user) // TODO: maybe a user view is not the most useful response here.
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/notificationtoken")
    fun removeNotificationToken(@PathVariable id: Long,
                                @AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.account.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")

        user.notificationToken = null
        userService.save(user)
        return UserView(user) // TODO: maybe a user view is not the most useful response here.
    }

    /**
     * GET /user/{id}/
     * Gets user with given id
     */
    @GetMapping("/{id}/")
    fun showUser(@PathVariable id: Long): BasicUserView {

        val user = userService.getUserById(id) ?: throw NotFoundException("user with id $id does not exist")
        return BasicUserView(user)
    }

    private fun User.setValuesFrom(userView: UserView): User {

        this.firstname = userView.firstname ?: this.firstname
        this.lastname = userView.lastname ?: this.lastname
        this.gender = userView.gender ?: this.gender

        userView.preferredLanguage?.let {
            when (it) {
                "en" -> this.preferredLanguage = Language.EN
                "de" -> this.preferredLanguage = Language.DE
                else -> logger.warn("Unsupported language locale $it")
            }
        }

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
    @GetMapping("/invitation")
    fun showInvitation(@RequestParam token: String): DetailedInvitationView {
        val invitation = teamService.findInvitationsByInviteCode(token) ?: throw NotFoundException("No invitation for code $token")
        return DetailedInvitationView(invitation)
    }
}
