package backend.exceptions;

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DomainException : Exception {

    constructor() : super()

    constructor(cause: Throwable) : super(cause)

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
