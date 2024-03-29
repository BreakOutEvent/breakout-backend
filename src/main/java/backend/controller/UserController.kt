package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.model.event.TeamService
import backend.model.media.Media
import backend.model.misc.Url
import backend.model.user.*
import backend.model.sponsoring.SupporterType
import backend.model.removeBlockedBy
import backend.model.removeBlocking
import backend.services.ConfigurationService
import backend.util.CacheNames.POSTINGS
import backend.util.CacheNames.TEAMS
import backend.view.DetailedInvitationView
import backend.view.NotificationTokenView
import backend.view.user.*
import io.swagger.annotations.Api
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.validation.Valid

@Api
@RestController
@RequestMapping("/user")
open class UserController(private val userService: UserService,
                          private val teamService: TeamService,
                          private val deletionService: DeletionService,
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
        val newsletter = body.newsletter

        if (userService.exists(email)) throw ConflictException("email ${body.email!!} already exists")

        val user = userService.create(email, password, newsletter)
        user.setValuesFrom(body)
        userService.save(user)

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
    fun showUsers(@AuthenticationPrincipal customUserDetails: CustomUserDetails?): Iterable<BasicUserView> {
        return userService.getAllUsers().removeBlockedBy(customUserDetails?.id).map(::BasicUserView)
    }

    /**
     * GET /user/search/{search}/
     * Searches for User by String greater 2 chars
     */
    @GetMapping("/search/{search}/")
    fun searchUsers(@PathVariable("search") search: String,
                    @AuthenticationPrincipal customUserDetails: CustomUserDetails?): List<SimpleUserView> {

        if (search.length < 3) return listOf()
        val user = customUserDetails?.id?.let { userService.getUserById(it) }
        val users = userService.searchByString(search).take(6).toMutableList()
        users.addAll(teamService.searchByString(search).take(3).flatMap { it.members.map { it.account } })

        return when (user?.hasRole(Admin::class)) {
            true -> users.map(::AdminSimpleUserView)
            else -> users.removeBlockedBy(customUserDetails?.id).removeBlocking(user).map(::SimpleUserView)
        }
    }

    /**
     * PUT /user/{id}/
     * Edits user with given id
     * Changes the user password
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(TEAMS, allEntries = true))])
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/")
    fun updateUser(@PathVariable id: Long,
                   @Valid @RequestBody body: UpdateUserView,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.account.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")

        user.setValuesFrom(body)

        if (!body.password.isNullOrBlank() && !body.newPassword.isNullOrBlank()) {
            if (!user.isCurrentPassword(body.password)) {
                throw BadRequestException("Current password is wrong")
            }
            user.setPassword(body.newPassword)
        }

        if (!body.email.isNullOrBlank()) {
            userService.requestEmailChange(user, body.email!!)
        }

        userService.save(user)
        return UserView(user)
    }

    /**
     * DELETE /user/{id}/
     * Edits user with given id
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(TEAMS, allEntries = true))])
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/")
    fun deleteUser(@PathVariable id: Long,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): UserView {

        val deleter = userService.getUserFromCustomUserDetails(customUserDetails)
        if (deleter.account.id != id && !deleter.hasRole(Admin::class))
            throw UnauthorizedException("authenticated user and requested resource mismatch")

        val user = userService.getUserById(id) ?: throw NotFoundException("user with id $id does not exist")
        deletionService.delete(user)

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
    fun showUser(@PathVariable id: Long,
                 @AuthenticationPrincipal customUserDetails: CustomUserDetails?): BasicUserView {

        val user = userService.getUserById(id) ?: throw NotFoundException("user with id $id does not exist")

        if (user.isBlockedBy(customUserDetails?.id))
            throw NotFoundException("user with id $id was blocked")

        return BasicUserView(user)
    }

    /**
     * POST /user/{id}/block
     * Blocks user with given id
     */
    @Caching(evict = arrayOf(
            CacheEvict(POSTINGS, allEntries = true),
            CacheEvict(TEAMS, allEntries = true)))
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/block/")
    fun blockUser(@PathVariable id: Long,
                  @AuthenticationPrincipal customUserDetails: CustomUserDetails): BasicUserView {

        val currentUser = userService.getUserFromCustomUserDetails(customUserDetails)
        val user = userService.getUserById(id) ?: throw NotFoundException("user with id $id does not exist")

        if (user.account.id == currentUser.account.id) throw BadRequestException("user not allowed to block itself")

        if (!user.isBlockedBy(currentUser.account.id))
            user.account.blockedBy.add(currentUser.account)

        userService.save(user)

        return BasicUserView(user)
    }

    /**
     * DELETE /user/{id}/block
     * Unblocks user with given idt
     */
    @Caching(evict = arrayOf(
            CacheEvict(POSTINGS, allEntries = true),
            CacheEvict(TEAMS, allEntries = true)))
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/block/")
    fun unblockUser(@PathVariable id: Long,
                    @AuthenticationPrincipal customUserDetails: CustomUserDetails): BasicUserView {

        val currentUser = userService.getUserFromCustomUserDetails(customUserDetails)
        val user = userService.getUserById(id) ?: throw NotFoundException("user with id $id does not exist")

        if (user.isBlockedBy(currentUser.account.id))
            user.account.blockedBy.remove(currentUser.account)

        userService.save(user)

        return BasicUserView(user)
    }

    private fun User.setValuesFrom(userView: UserView): User {

        this.firstname = userView.firstname ?: this.firstname
        this.lastname = userView.lastname ?: this.lastname
        this.gender = userView.gender ?: this.gender
        this.profilePic = userView.profilePic?.let(::Media) ?: this.profilePic
        this.newsletter = userView.newsletter ?: this.newsletter

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

        sponsorView.supporterType?.let {
            when (it) {
                "DONOR" -> sponsor.supporterType = SupporterType.DONOR
                "ACTIVE" -> sponsor.supporterType = SupporterType.ACTIVE
                "PASSIVE" -> sponsor.supporterType = SupporterType.PASSIVE
                else -> SupporterType.NONE
            }
        }
        sponsor.address = sponsorView.address?.toAddress() ?: sponsor.address
        sponsor.isHidden = sponsorView.isHidden ?: sponsor.isHidden
        sponsor.company = sponsorView.company ?: sponsor.company
        sponsor.logo = sponsorView.logo?.let(::Media) ?: sponsor.logo

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
        val invitation = teamService.findInvitationsByInviteCode(token)
                ?: throw NotFoundException("No invitation for code $token")
        return DetailedInvitationView(invitation)
    }

    @GetMapping("/exists")
    fun exists(@RequestParam email: String): Boolean {
        return userService.exists(email)
    }

    /**
     * DELETE /user/{id}/logo
     * Deletes company logo of given sponsor
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(TEAMS, allEntries = true))])
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/logo")
    fun deleteLogo(@PathVariable id: Long,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): BasicUserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.account.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")

        val sponsor: Sponsor = user.getRole(Sponsor::class) ?: user.addRole(Sponsor::class)
        sponsor.logo = null
        userService.save(user)

        return BasicUserView(user)
    }

    /**
     * DELETE /user/{id}/url
     * Deletes company url of given sponsor
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(TEAMS, allEntries = true))])
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/url")
    fun deleteUrl(@PathVariable id: Long,
                  @AuthenticationPrincipal customUserDetails: CustomUserDetails): BasicUserView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (user.account.id != id) throw UnauthorizedException("authenticated user and requested resource mismatch")

        val sponsor: Sponsor = user.getRole(Sponsor::class) ?: user.addRole(Sponsor::class)
        sponsor.url = null
        userService.save(user)

        return BasicUserView(user)
    }

}
