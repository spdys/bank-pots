package banking

import banking.dto.FailureResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class GlobalExceptionHandler {

    // Handling bad requests
    @ExceptionHandler(BankingBadRequestException::class)
    fun handleBankingBadRequestException(ex: BankingBadRequestException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FailureResponse(ex.message ?: "Bad request."))
    }

    // Handling not founds
    @ExceptionHandler(BankingNotFoundException::class)
    fun handleBankingNotFoundException(ex: BankingNotFoundException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(FailureResponse(ex.message ?: "Not found."))
    }

        // Handling forbiddens
        @ExceptionHandler(BankingForbiddenException::class)
        fun handleBankingForbiddenException(ex: BankingForbiddenException): ResponseEntity<FailureResponse> {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(FailureResponse(ex.message ?: "Forbidden."))
    }
}