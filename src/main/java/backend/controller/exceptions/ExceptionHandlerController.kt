package backend.controller.exceptions

import backend.exceptions.DomainException
import org.apache.log4j.Logger
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@ControllerAdvice
class ExceptionHandlerController {

    private val logger = Logger.getLogger(ExceptionHandlerController::class.java)

    // Used for handling Jackson Parsing Exceptions
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errorMessage = createHumanErrorMessage(e)
        val response = mapOf(
                "timestamp" to LocalDateTime.now().toEpochSecond(UTC).toString(),
                "message" to errorMessage
        )
        return ResponseEntity(response, BAD_REQUEST)
    }

    fun createHumanErrorMessage(e: MethodArgumentNotValidException): String {
        val fieldErrors = e.bindingResult.fieldErrors
        return fieldErrors.map { "${it.field} ${it.defaultMessage}" }.reduce { first, second -> "$first; $second" }
    }

    @ExceptionHandler(Exception::class)
    fun handle(e: DomainException): ResponseEntity<Map<String, String>> {
        logger.info("A domain exception with cause ${e.message} was returned as BadRequest to user")
        val message = mapOf(
                "timestamp" to LocalDateTime.now().toEpochSecond(UTC).toString(),
                "status"    to BAD_REQUEST.value().toString(),
                "error"     to "Bad Request",
                "message"   to (e.message ?: "Something went wrong with your request"))
        return ResponseEntity(message, BAD_REQUEST)
    }
}

