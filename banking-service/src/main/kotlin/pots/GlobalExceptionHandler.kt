package pots

import pots.dto.FailureResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PotsException::class)
    fun handlePotsException(ex: PotsException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FailureResponse(ex.message ?: "An error occurred."))
    }
}