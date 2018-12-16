package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.*
import backend.model.challenges.ChallengeService
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.misc.Email
import backend.model.misc.EmailAddress
import backend.model.posting.PostingService
import backend.model.user.UserService
import backend.model.removeBlockedBy
import backend.model.removeReported
import backend.model.user.Admin
import backend.services.ConfigurationService
import backend.services.mail.MailSenderService
import backend.util.CacheNames.LOCATIONS
import backend.util.CacheNames.POSTINGS
import backend.util.localDateTimeOf
import backend.view.CommentView
import backend.view.LikeView
import backend.view.LocationView
import backend.view.posting.PostingResponseView
import backend.view.posting.PostingView
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.DELETE
import javax.validation.Valid

@RestController
@RequestMapping("/posting")
class PostingController(private val postingService: PostingService,
                        private val configurationService: ConfigurationService,
                        private val userService: UserService,
                        private val challengeService: ChallengeService,
                        private val mailSenderService: MailSenderService) {

    private val logger = LoggerFactory.getLogger(PostingController::class.java)

    private val JWT_SECRET: String = configurationService.getRequired("org.breakout.api.jwt_secret")
    private val PAGE_SIZE: Int = configurationService.getRequired("org.breakout.api.page_size").toInt()


    /**
     * POST /posting/
     * Creates new posting
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(LOCATIONS, allEntries = true))])
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun createPosting(@Valid @RequestBody body: PostingView,
                      @AuthenticationPrincipal customUserDetails: CustomUserDetails): PostingView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)

        val postingLocation = body.postingLocation
        val locationCoord = when (postingLocation) {
            is LocationView -> Coord(postingLocation.latitude, postingLocation.longitude)
            else -> null
        }

        val clientDate = localDateTimeOf(body.date ?: throw RuntimeException("Client date has not been given"))

        val posting = postingService.createPosting(user, body.text, body.media?.let(::Media), locationCoord, clientDate)

        return PostingView(posting, null, user.account.id)
    }

    /**
     * GET /posting/{id}/
     * Gets posting by id
     */
    @GetMapping("/{id}/")
    fun getPosting(@PathVariable("id") id: Long,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails?): PostingResponseView {

        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val user = customUserDetails?.let { userService.getUserFromCustomUserDetails(it) }
        val isAdmin = user?.hasRole(Admin::class) ?: false

        if (posting.isBlockedBy(customUserDetails?.id))
            throw NotFoundException("posting with id $id was posted by blocked user ${posting.user!!.id}")

        if (posting.reported && !isAdmin)
            throw NotFoundException("posting with id $id was reported.")

        val challengeProveProjection = posting.challenge?.let {
            challengeService.findChallengeProveProjectionById(posting.challenge!!)
        }
        return PostingResponseView(posting.hasLikesBy(customUserDetails?.id),
                                   challengeProveProjection,
                                   customUserDetails?.id)
    }

    /**
     * GET /posting/report/
     * Gets posting by id
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/report/")
    fun getReported(@AuthenticationPrincipal customUserDetails: CustomUserDetails?): Iterable<PostingResponseView> {
        return postingService.findReported().map {
            PostingResponseView(it.hasLikesBy(customUserDetails?.id), it.challenge?.let {
                challengeService.findChallengeProveProjectionById(it)
            }, customUserDetails?.id)
        }
    }

    /**
     * DELETE /posting/{id}/report/
     * Allows Admin to delete report about a Posting
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(LOCATIONS, allEntries = true))])
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}/report/")
    fun dismissReport(@PathVariable("id") id: Long): PostingResponseView {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")

        posting.reported = false
        postingService.save(posting)

        val challengeProveProjection = posting.challenge?.let {
            challengeService.findChallengeProveProjectionById(posting.challenge!!)
        }

        return PostingResponseView(posting, challengeProveProjection,null)
    }

    /**
     * DELETE /posting/{id}/
     * Allows Admin or poster to delete Posting
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(LOCATIONS, allEntries = true))])
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/", method = [DELETE])
    fun deletePosting(@PathVariable("id") id: Long, @AuthenticationPrincipal customUserDetails: CustomUserDetails
    ): Map<String, String> {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        if (!(user.hasRole(Admin::class)) && posting.user?.id != customUserDetails.id) {
            throw UnauthorizedException("A user can only delete postings submitted by itself")
        }

        posting.challenge?.let {
            challengeService.rejectProof(challengeService.findOne(posting.challenge!!)!!)
        }
        postingService.delete(posting)
        return mapOf("message" to "success")
    }

    /**
     * DELETE /posting/{id}/comment/{commentId}/
     * Allows Admin to delete Comment
     */
    @CacheEvict(value = POSTINGS, allEntries = true)
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/{id}/comment/{commentId}/", method = [DELETE])
    fun adminDeleteComment(@PathVariable("id") postingId: Long,
                           @PathVariable("commentId") commentId: Long): Map<String, String> {

        val posting = postingService.getByID(postingId) ?: throw NotFoundException("Posting with id $postingId not found")
        postingService.removeComment(from = posting, id = commentId)

        return mapOf("message" to "success")
    }


    /**
     * GET /posting/
     * Gets all postings
     */
    @Cacheable(POSTINGS, sync = true)
    @GetMapping("/")
    fun getAllPostings(@RequestParam(value = "page", required = false) page: Int?,
                       @AuthenticationPrincipal customUserDetails: CustomUserDetails?,
                       @RequestParam(value = "event", required = false) events: List<Long>?): Iterable<PostingResponseView> {

        logger.info("Cache miss on /posting for page $page userId $customUserDetails?.id events $events")

        val postings = if(events != null) {
            postingService.findByEventIds(events, page ?: 0 , PAGE_SIZE)
        } else {
            postingService.findAll(page ?: 0, PAGE_SIZE)
        }

        return postings.removeReported().removeBlockedBy(customUserDetails?.id).map {
            PostingResponseView(it.hasLikesBy(customUserDetails?.id), it.challenge?.let {
                challengeService.findChallengeProveProjectionById(it)
            }, customUserDetails?.id)
        }
    }

    /**
     * POST /posting/{id}/report/
     * Gets posting by id
     */
    @Caching(evict = [(CacheEvict(POSTINGS, allEntries = true)), (CacheEvict(LOCATIONS, allEntries = true))])
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/report/")
    fun reportPosting(@PathVariable("id") id: Long,
                      @AuthenticationPrincipal customUserDetails: CustomUserDetails?): PostingResponseView {

        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")

        if (posting.reported)
            throw ConflictException("posting with id $id was already reported.")

        posting.reported = true
        postingService.save(posting)

        val admins = userService.getAllAdmins().map { EmailAddress(it.email) }
        val email = Email(admins, "Posting was reported", "Posting $id by ${posting.team!!.name} was reported.")
        mailSenderService.send(email)

        val challengeProveProjection = posting.challenge?.let {
            challengeService.findChallengeProveProjectionById(posting.challenge!!)
        }

        return PostingResponseView(posting.hasLikesBy(customUserDetails?.id),
                challengeProveProjection,
                customUserDetails?.id)
    }

    /**
     * POST /posting/{id}/comment/
     * creates Comment for Posting
     */
    @CacheEvict(value = POSTINGS, allEntries = true)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/comment/")
    @ResponseStatus(CREATED)
    fun createComment(@PathVariable("id") id: Long,
                      @Valid @RequestBody body: CommentView,
                      @AuthenticationPrincipal customUserDetails: CustomUserDetails): CommentView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")

        val comment = postingService.addComment(
                to = posting,
                from = user.account,
                at = localDateTimeOf(body.date!!),
                withText = body.text)

        return CommentView(comment)
    }


    /**
     * POST /posting/{id}/like/
     * creates Like for Posting
     */
    @CacheEvict(value = POSTINGS, allEntries = true)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like/")
    @ResponseStatus(CREATED)
    fun createLike(@PathVariable("id") id: Long,
                   @Valid @RequestBody body: LikeView,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): LikeView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val like = postingService.like(posting, user.account, localDateTimeOf(body.date!!))

        return LikeView(like)
    }


    /**
     * DELETE /posting/{id}/like/
     * deletes Like for Posting
     */
    @CacheEvict(value = POSTINGS, allEntries = true)
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/like/", method = [DELETE])
    fun deleteLike(@PathVariable("id") id: Long,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): Map<String, String> {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        postingService.unlike(by = user.account, from = posting)

        return mapOf("message" to "success")
    }

    /**
     * GET /posting/{id}/like/
     * Gets Likes for Posting
     */
    @GetMapping("/{id}/like/")
    fun getLikesForPosting(@PathVariable("id") id: Long,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails?): List<LikeView> {

        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val likes = posting.likes
        return likes.removeBlockedBy(customUserDetails?.id).map(::LikeView)
    }

    /**
     * GET /posting/hashtag/{hashtag}/
     * Gets Likes for Posting
     */
    @GetMapping("/hashtag/{hashtag}/")
    fun getPostingsByHashtag(@RequestParam(value = "page", required = false) page: Int?,
                             @PathVariable("hashtag") hashtag: String,
                             @AuthenticationPrincipal customUserDetails: CustomUserDetails?): List<PostingView> {

        val posting = postingService.findByHashtag(hashtag, page ?: 0, PAGE_SIZE)
        return posting.removeReported().removeBlockedBy(customUserDetails?.id).map {
            PostingView(it.hasLikesBy(customUserDetails?.id), it.challenge?.let {
                challengeService.findOne(it)
            }, customUserDetails?.id)
        }
    }

}
