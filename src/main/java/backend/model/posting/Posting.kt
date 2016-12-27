package backend.model.posting

import backend.controller.exceptions.ConflictException
import backend.model.BasicEntity
import backend.model.challenges.Challenge
import backend.model.location.Location
import backend.model.media.Media
import backend.model.user.UserCore
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.persistence.*
import javax.persistence.CascadeType.PERSIST

@Entity
class Posting : BasicEntity {

    private constructor() : super()

    @Column(columnDefinition = "TEXT")
    var text: String? = null

    @ElementCollection
    var hashtags: List<Hashtag> = ArrayList()

    lateinit var date: LocalDateTime

    @OneToOne(cascade = arrayOf(PERSIST))
    var location: Location? = null

    @OneToOne(cascade = arrayOf(PERSIST))
    var challenge: Challenge? = null

    @ManyToOne
    var user: UserCore? = null

    @OneToMany(cascade = arrayOf(CascadeType.ALL))
    var media: MutableList<Media> = arrayListOf()

    @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "posting", orphanRemoval = true)
    var comments: MutableList<Comment> = arrayListOf()

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    @JoinTable(
            joinColumns = arrayOf(JoinColumn(name = "posting_id", referencedColumnName = "id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "like_id", referencedColumnName = "id"))
    )
    var likes: MutableSet<Like> = hashSetOf()

    @Transient
    var hasLiked = false

    constructor(text: String?, date: LocalDateTime, location: Location?, user: UserCore, media: MutableList<Media>) : this() {
        this.text = text
        this.date = date
        this.location = location
        this.user = user
        this.media = media
        if (text != null) this.hashtags = extractHashtags(text)
    }

    private fun extractHashtags(text: String): List<Hashtag> {
        val pattern: Pattern = Pattern.compile("\\#([^\\s]*)")
        val matcher: Matcher = pattern.matcher(text)

        val hashtags = ArrayList<Hashtag>()

        while (matcher.find()) {
            val hashtag = matcher.group(1)
            hashtags.add(Hashtag(hashtag))
        }

        return hashtags
    }

    fun like(createdAt: LocalDateTime, user: UserCore): Like {
        val like = Like(createdAt, user)

        if (this.isLikedBy(user)) {
            throw ConflictException("User ${user.id} has already liked this posting!")
        } else {
            this.likes.add(like)
        }

        return like
    }

    fun unlike(user: UserCore) {
        if (this.isLikedBy(user)) {
            val like = findLikeByUser(user)
            this.likes.remove(like)
        } else {
            throw ConflictException("Can't unlike because user ${user.id} has not liked posting ${this.id}")
        }
    }

    private fun findLikeByUser(user: UserCore): Like? {
        return this.likes
                .filter { it.user?.id == user.id } // TODO: use equals here somehow?
                .firstOrNull()
    }

    private fun isLikedBy(user: UserCore): Boolean {
        return findLikeByUser(user) != null
    }


    @PreRemove
    fun preRemove() {
        this.likes.clear()
        this.comments.clear()
        this.media.clear()
        this.challenge = null
        this.user = null
    }

    // TODO: Refactor this!
    fun hasLikesBy(userId: Long?): Posting {
        if (userId != null) {
            this.hasLiked = this.likes.any { it.user?.id == userId }
        }
        return this
    }
}
