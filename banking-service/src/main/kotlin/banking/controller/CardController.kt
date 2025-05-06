package com.banking.bankingservice.controller

import banking.dto.CardDTO

import com.banking.bankingservice.entity.CardEntity
import com.banking.bankingservice.service.CardService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cards")
class CardController(
    private val cardService: CardService
) {

    @PostMapping
    fun createCard(@RequestBody request: CardDTO): ResponseEntity<CardEntity> {
        val newCard = cardService.createCard(request)
        return ResponseEntity(newCard, HttpStatus.CREATED)
    }
}
