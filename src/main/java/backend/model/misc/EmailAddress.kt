package backend.model.misc

import backend.exceptions.DomainException
import org.apache.commons.validator.routines.EmailValidator
import org.hibernate.validator.constraints.Email
import javax.persistence.Embeddable

@Embeddable
class EmailAddress() {

    @Email
    private lateinit var value: String

    constructor(email: String) : this() {
        val validator = EmailValidator.getInstance()
        if (!validator.isValid(email)) throw DomainException("Invalid email $email")
        this.value = email
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        if ((other !is EmailAddress)) return false
        val o = other.value.toLowerCase()
        val t = this.value.toLowerCase()
        return o == t
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
