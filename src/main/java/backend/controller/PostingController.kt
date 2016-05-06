package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.model.media.MediaService
import backend.model.posting.CommentService
import backend.model.posting.LikeService
import backend.model.posting.PostingService
import backend.model.user.UserService
import backend.services.ConfigurationService
import backend.util.getSignedJwtToken
import backend.util.toLocalDateTime
import backend.view.CommentView
import backend.view.LikeView
import backend.view.PostingView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

@RestController
@RequestMapping("/posting")
open class PostingController {

    private val mediaService: MediaService
    private val commentService: CommentService
    private val likeService: LikeService
    private val postingService: PostingService
    private val configurationService: ConfigurationService
    private val logger: Logger
    private var JWT_SECRET: String
    private val userService: UserService

    @Autowired
    constructor(postingService: PostingService,
                commentService: CommentService,
                likeService: LikeService,
                mediaService: MediaService,
                configurationService: ConfigurationService,
                userService: UserService) {

        this.postingService = postingService
        this.commentService = commentService
        this.likeService = likeService
        this.mediaService = mediaService
        this.configurationService = configurationService
        this.logger = LoggerFactory.getLogger(PostingController::class.java)
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
        this.userService = userService
    }


    /**
     * POST /posting/
     * Creates new posting
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createPosting(@Valid @RequestBody body: PostingView,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): PostingView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)

        val posting = postingService.createPosting(user, body.text, body.uploadMediaTypes, body.postingLocation, body.date)
        posting.media?.forEach { it.uploadToken = getSignedJwtToken(JWT_SECRET, it.id.toString()) }

        return PostingView(posting)
    }

    /**
     * GET /posting/{id}/
     * Gets posting by id
     */
    @RequestMapping("/{id}/", method = arrayOf(GET))
    open fun getPosting(@PathVariable("id") id: Long): PostingView {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        return PostingView(posting)
    }

    /**
     * GET /posting/
     * Gets all postings
     */
    @RequestMapping("/", method = arrayOf(GET))
    open fun getAllPostings(): Iterable<PostingView> {
        return postingService.findAll().map { PostingView(it) }
    }

    /**
     * POST /posting/get/ids/
     * Gets postings with given ids
     */
    @RequestMapping("/get/ids", method = arrayOf(POST))
    open fun getPostingsById(@Valid @RequestBody body: List<Long>): Iterable<PostingView> {
        return postingService.findAllByIds(body).map { PostingView(it) }
    }

    /**
     * GET /posting/get/since/id/
     * Gets all postings after a given posting
     */
    @RequestMapping("/get/since/{id}/", method = arrayOf(GET))
    open fun getPostingIdsSince(@PathVariable("id") id: Long): Iterable<Long> {
        return postingService.findAllSince(id).map { it.id!! }
    }

    /**
     * POST /posting/{id}/comment/
     * creates Comment for Posting
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/comment/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createComment(@PathVariable("id") id: Long,
                           @Valid @RequestBody body: CommentView,
                           @AuthenticationPrincipal customUserDetails: CustomUserDetails): CommentView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val comment = commentService.createComment(body.text, body.date!!.toLocalDateTime(), posting, user.core)

        return CommentView(comment)
    }


    /**
     * POST /posting/{id}/like/
     * creates Like for Posting
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/{id}/like/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    open fun createLike(@PathVariable("id") id: Long,
                        @Valid @RequestBody body: LikeView,
                        @AuthenticationPrincipal customUserDetails: CustomUserDetails): LikeView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val like = likeService.createLike(body.date!!.toLocalDateTime(), posting, user.core)

        return LikeView(like)
    }

    /**
     * GET /posting/{id}/like/
     * Gets Likes for Posting
     */
    @RequestMapping("/{id}/like/", method = arrayOf(GET))
    open fun getLikesForPosting(@PathVariable("id") id: Long): List<LikeView> {
        val posting = postingService.getByID(id) ?: throw NotFoundException("posting with id $id does not exist")
        val likes = likeService.findAllByPosting(posting)
        return likes.map { LikeView(it) }
    }
}
