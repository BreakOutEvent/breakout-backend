package backend.model.posting

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

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var media: MutableList<Media>? = ArrayList()

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var comments: MutableList<Comment> = ArrayList()

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    var likes: MutableList<Like> = ArrayList()

    @Transient
    var hasLiked = false

    constructor(text: String?, date: LocalDateTime, location: Location?, user: UserCore, media: MutableList<Media>?) : this() {
        this.text = text
        this.date = date
        this.location = location
        this.user = user
        this.media = media
        if (text != null) this.hashtags = extractHashtags(text)
    }

    private fun extractHashtags(text: String): List<Hashtag> {
        val pattern: Pattern = Pattern.compile("\\#(\\w+)");
        val matcher: Matcher = pattern.matcher(text)

        val hashtags = ArrayList<Hashtag>()

        while (matcher.find()) {
            val hashtag = matcher.group(1)
            hashtags.add(Hashtag(hashtag))
        }

        return hashtags
    }


    @PreRemove
    fun preRemove() {
        this.likes.clear()
        this.comments.clear()
        this.media?.clear()
        this.challenge = null
        this.user = null
    }

    fun hasLikesBy(userId: Long?): Posting {
        if (userId != null) {
            this.hasLiked = this.likes.any { it.user?.id == userId }
        }
        return this
    }
}
