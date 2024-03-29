package backend.model.posting

import backend.model.UserGenerated
import backend.controller.exceptions.BadRequestException
import backend.controller.exceptions.ConflictException
import backend.controller.exceptions.NotFoundException
import backend.model.BasicEntity
import backend.model.Reportable
import backend.model.event.Team
import backend.model.location.Location
import backend.model.media.Media
import backend.model.user.Participant
import backend.model.user.User
import backend.model.user.UserAccount
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.persistence.CascadeType.PERSIST
import javax.persistence.FetchType.LAZY

@Entity
class Posting : BasicEntity, UserGenerated, Reportable {

    private constructor() : super()

    @Column(columnDefinition = "TEXT")
    var text: String? = null

    /*
    TODO: Move hashtags to own entity - github issue #194
     */
    @ElementCollection
    var hashtags: List<Hashtag> = ArrayList()

    lateinit var date: LocalDateTime

    @OneToOne(cascade = [PERSIST])
    var location: Location? = null

    @Column(name = "challenge_id")
    var challenge: Long? = null

    @ManyToOne
    var user: UserAccount? = null

    @ManyToOne(fetch = LAZY)
    var team: Team? = null

    @OneToOne(cascade = [(ALL)])
    var media: Media? = null

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var comments: MutableList<Comment> = arrayListOf()

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true)
    @JoinTable(
            joinColumns = [(JoinColumn(name = "posting_id", referencedColumnName = "id"))],
            inverseJoinColumns = [(JoinColumn(name = "like_id", referencedColumnName = "id"))]
    ) // explicit annotation because "like" would cause an error as it is an sql keyword
    var likes: MutableSet<Like> = hashSetOf()

    @Transient
    var hasLiked = false

    override var reported: Boolean = false

    constructor(text: String?, date: LocalDateTime, location: Location?, user: UserAccount, media: Media?) : this() {
        this.text = text
        this.date = date
        this.location = location
        this.user = user
        this.media = media
        this.team = user.getRole(Participant::class)?.getCurrentTeam()
        if (text != null) this.hashtags = extractHashtags(text)
    }

    private fun extractHashtags(text: String): List<Hashtag> {
        val pattern: Pattern = Pattern.compile("#([^\\s]*)")
        val matcher: Matcher = pattern.matcher(text)

        val hashtags = ArrayList<Hashtag>()

        while (matcher.find()) {
            val hashtag = matcher.group(1)
            hashtags.add(Hashtag(hashtag))
        }

        return hashtags
    }

    fun like(createdAt: LocalDateTime, user: UserAccount): Like {
        val like = Like(createdAt, user)

        if (this.isLikedBy(user)) {
            throw ConflictException("User ${user.id} has already liked this posting!")
        } else {
            this.likes.add(like)
        }

        return like
    }

    fun unlike(user: UserAccount) {
        if (this.isLikedBy(user)) {
            val like = findLikeByUser(user)
            this.likes.remove(like)
        } else {
            throw ConflictException("Can't unlike because user ${user.id} has not liked posting ${this.id}")
        }
    }

    private fun findLikeByUser(user: UserAccount): Like? {
        return this.likes.firstOrNull { it.user?.id == user.id }
    }

    private fun isLikedBy(user: UserAccount): Boolean {
        return findLikeByUser(user) != null
    }


    @PreRemove
    fun preRemove() {
        this.likes.clear()
        this.comments.clear()
        this.media = null
        this.challenge = null
        this.user = null
        this.team = null
    }

    // TODO: Refactor this!
    fun hasLikesBy(userId: Long?): Posting {
        if (userId != null) {
            this.hasLiked = this.likes.any { it.user?.id == userId }
        }
        return this
    }

    public fun findCommentById(commentId: Long): Comment? {
        return this.comments.firstOrNull { it.id == commentId }
    }

    fun removeComment(commentId: Long) {

        this.findCommentById(commentId)?.let {
            this.comments.remove(it)
            return
        }

        throw NotFoundException("Comment with id $commentId not found at posting $id")
    }

    fun addComment(from: UserAccount, at: LocalDateTime, withText: String): Comment {

        if (withText.trim().isEmpty()) {
            throw BadRequestException("Empty comments are not allowed")
        }

        val comment = Comment(withText, at, from)
        this.comments.add(comment)
        return comment
    }

    override fun getUser(): User? {
        return user
    }

}
