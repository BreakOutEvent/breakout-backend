package backend.model.posting

import javax.persistence.Embeddable

@Embeddable
class Hashtag() {

    lateinit var value: String

    constructor(hashtag: String) : this() {
        this.value = hashtag
    }

    override fun toString(): String {
        return value
    }
}
