package com.banking.bankingservice.controller

import com.banking.bankingservice.entity.CardEntity
import com.banking.bankingservice.service.CardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cards")
class CardController(
    private val cardService: CardService
) {

    @GetMapping("/{id}")
    fun getCardById(@PathVariable id: Long): ResponseEntity<CardEntity> {
        val card = cardService.getCardById(id)
        return ResponseEntity.ok(card)
    }

    @DeleteMapping("/{id}")
    fun deleteCard(@PathVariable id: Long): ResponseEntity<Void> {
        cardService.deleteCard(id)
        return ResponseEntity.noContent().build()
    }
}
