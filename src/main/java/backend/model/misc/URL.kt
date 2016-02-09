package backend.model.misc

import org.apache.commons.validator.routines.UrlValidator

data class URL(private val from: String) {
    init {
        if (!UrlValidator().isValid(from)) throw Exception("$from is no valid URL")
    }

    override fun toString() = this.from
}
