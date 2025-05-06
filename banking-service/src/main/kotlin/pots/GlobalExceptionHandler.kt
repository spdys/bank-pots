package pots

import jakarta.persistence.EntityNotFoundException
import pots.dto.FailureResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class GlobalExceptionHandler {

    // Handle PotsExceptions
    @ExceptionHandler(PotsException::class)
    fun handlePotsException(ex: PotsException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FailureResponse(ex.message ?: "An error occurred."))
    }

    // Handling IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FailureResponse(ex.message ?: "Invalid argument provided."))
    }

    // Handling EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(FailureResponse(ex.message ?: "Entity not found."))
    }

    // Catch other unhandled exceptions and return a generic message
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<FailureResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(FailureResponse(ex.message ?: "An unexpected error occurred."))
    }
}