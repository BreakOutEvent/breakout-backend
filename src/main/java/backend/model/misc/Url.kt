package backend.model.misc

import org.hibernate.validator.constraints.URL
import javax.persistence.Embeddable

@Embeddable
class Url() {

    @URL
    private lateinit var value: String

    constructor(value: String) : this() {
        this.value = value
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        if ((other !is Url)) return false
        return other.value == this.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
