package backend.model.misc

import org.apache.commons.validator.EmailValidator
import org.hibernate.validator.constraints.Email
import javax.persistence.Embeddable

@Embeddable
class EmailAddress {

    @Email
    private val value: String

    constructor(email: String) {
        val validator = EmailValidator.getInstance()
        if (!validator.isValid(email)) throw Exception("Invalid email $email")
        this.value = email
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        if ((other !is EmailAddress)) return false
        return other.value == this.value
    }

    override fun hashCode(): Int{
        return value.hashCode()
    }
}
