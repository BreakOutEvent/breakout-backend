package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.model.challenges.ChallengeService
import backend.model.media.Media
import backend.model.misc.Coord
import backend.model.posting.PostingService
import backend.model.user.UserService
import backend.removeBlockedBy
import backend.services.ConfigurationService
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
                        private val challengeService: ChallengeService) {

    private val logger = LoggerFactory.getLogger(PostingController::class.java)

    private val JWT_SECRET: String = configurationService.getRequired("org.breakout.api.jwt_secret")
    private val PAGE_SIZE: Int = configurationService.getRequired("org.breakout.api.page_size").toInt()


    /**
     * POST /posting/
     * Creates new posting
     */
    @Caching(evict = arrayOf(
            CacheEvict(POSTINGS, allEntries = true),
            CacheEvict(LOCATIONS, allEntries = true)))
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
                   @RequestParam(value = "userid", required = false) userId: Long?): PostingResponseView {

        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")

        if (posting.isBlockedBy(userId))
            throw NotFoundException("posting with id $id was posted by blocked user ${posting.user!!.id}")

        val challengeProveProjection = posting.challenge?.let {
            challengeService.findChallengeProveProjectionById(posting.challenge!!)
        }
        return PostingResponseView(posting.hasLikesBy(userId), challengeProveProjection, userId)
    }

    /**
     * DELETE /posting/{id}/
     * Allows Admin to delete Posting
     */
    @Caching(evict = arrayOf(
            CacheEvict(POSTINGS, allEntries = true),
            CacheEvict(LOCATIONS, allEntries = true)))
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/{id}/", method = arrayOf(DELETE))
    fun adminDeletePosting(@PathVariable("id") id: Long): Map<String, String> {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
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
    @RequestMapping("/{id}/comment/{commentId}/", method = arrayOf(DELETE))
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
                       @RequestParam(value = "userid", required = false) userId: Long?,
                       @RequestParam(value = "event", required = false) events: List<Long>?): Iterable<PostingResponseView> {

        logger.info("Cache miss on /posting for page $page userId $userId events $events")

        val postings = if(events != null) {
            postingService.findByEventIds(events, page ?: 0 , PAGE_SIZE)
        } else {
            postingService.findAll(page ?: 0, PAGE_SIZE)
        }

        return postings.removeBlockedBy(userId).map {
            PostingResponseView(it.hasLikesBy(userId), it.challenge?.let {
                challengeService.findChallengeProveProjectionById(it)
            }, userId)
        }
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
     * creates Like for Posting
     */
    @CacheEvict(value = POSTINGS, allEntries = true)
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/like/", method = arrayOf(DELETE))
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
    fun getLikesForPosting(@PathVariable("id") id: Long): List<LikeView> {

        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val likes = posting.likes
        return likes.map(::LikeView)
    }

    /**
     * GET /posting/hashtag/{hashtag}/
     * Gets Likes for Posting
     */
    @GetMapping("/hashtag/{hashtag}/")
    fun getPostingsByHashtag(@RequestParam(value = "page", required = false) page: Int?,
                             @PathVariable("hashtag") hashtag: String,
                             @RequestParam(value = "userid", required = false) userId: Long?): List<PostingView> {

        val posting = postingService.findByHashtag(hashtag, page ?: 0, PAGE_SIZE)
        return posting.removeBlockedBy(userId).map {
            PostingView(it.hasLikesBy(userId), it.challenge?.let {
                challengeService.findOne(it)
            }, userId)
        }
    }

}
