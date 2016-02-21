package backend.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@ControllerAdvice
class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errorMessage = createHumanErrorMessage(e)
        val response = mapOf(
                "timestamp" to LocalDateTime.now().toEpochSecond(UTC).toString(),
                "message" to errorMessage
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    fun createHumanErrorMessage(e: MethodArgumentNotValidException): String {
        val fieldErrors = e.bindingResult.fieldErrors
        return fieldErrors.map { "${it.field} ${it.defaultMessage}" }.reduce { first, second -> "$first; $second" }
    }
}

