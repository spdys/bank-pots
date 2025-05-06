package banking

import banking.dto.FailureResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class GlobalExceptionHandler {

    // Handling bad requests
    @ExceptionHandler(BankingBadRequestException::class)
    fun handlePotsBadRequestException(ex: BankingBadRequestException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FailureResponse(ex.message ?: "Bad request."))
    }

    // Handling not founds
    @ExceptionHandler(BankingNotFoundException::class)
    fun handlePotsNotFoundException(ex: BankingNotFoundException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(FailureResponse(ex.message ?: "Not found."))
    }

    // Catch other unhandled exceptions and return a generic message
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FailureResponse(ex.message ?: "An unexpected error occurred."))
    }
}