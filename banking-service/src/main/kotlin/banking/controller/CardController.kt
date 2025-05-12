package banking.controller

import banking.entity.CardEntity
import banking.service.CardService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import banking.dto.FailureResponse
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@Tag(name = "Card API")
@RequestMapping("/cards")
class CardController(
    private val cardService: CardService
) {

//    @Operation(summary = "Get card by ID")
//    @GetMapping("/{id}")
//    fun retrieveCardById(@PathVariable id: Long): ResponseEntity<CardEntity> {
//        val card = cardService.getCardById(id)
//        return ResponseEntity.ok(card)
//    }

    @Operation(summary = "Delete card by ID")
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "Card successfully deleted."),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden: only admins may perform this action.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))]),
        ApiResponse(
            responseCode = "404",
            description = "Card not found.",
            content = [Content(schema = Schema(implementation = FailureResponse::class))])
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteCard(@PathVariable id: Long): ResponseEntity<Void> {
        cardService.deleteCard(id)
        return ResponseEntity.noContent().build()
    }
}
