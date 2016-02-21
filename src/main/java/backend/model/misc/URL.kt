package backend.model.misc

import backend.exceptions.DomainException
import org.apache.commons.validator.routines.UrlValidator

data class URL(private val from: String) {
    init {
        if (!UrlValidator().isValid(from)) throw DomainException("$from is no valid URL")
    }

    override fun toString() = this.from
}
