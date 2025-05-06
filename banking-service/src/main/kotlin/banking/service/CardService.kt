package com.banking.bankingservice.service

import banking.MissingAccountException
import banking.dto.CardDTO
import com.banking.bankingservice.entity.Account
import com.banking.bankingservice.entity.CardEntity
import com.banking.bankingservice.entity.Pot
import com.banking.bankingservice.repository.CardRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random


@Service
class CardService(
    private val cardRepository: CardRepository
) {
    fun createCard(request: CardDTO): CardEntity {
        val tempAccount = Account(tempId = request.accountId ?: throw MissingAccountException()) // to be replaced

        val tempPot = request.potId?.let { Pot(tempId = it) } // to be replaced

        val cardNumber = generateCardNumber()

        val token = if (request.cardType.uppercase() == "VIRTUAL") generateToken() else null

        val now = LocalDateTime.now()
        val expiresAt = now.plusYears(5)

        val card = CardEntity(
            cardNumber = cardNumber,
            token = token,
            cardType = request.cardType.uppercase(),
            isActive = true,
            createdAt = now,
            expiresAt = expiresAt,
            account = tempAccount,
            pot = tempPot
        )

        return cardRepository.save(card)
    }

    private fun generateCardNumber(): String {
        val firstDigit = (1..9).random()
        val remainingDigits = (1..15).map { Random.nextInt(0, 10) }.joinToString("")
        return "$firstDigit$remainingDigits"
    }

    private fun generateToken(): String {
        return UUID.randomUUID().toString().replace("-", "").take(16)
    }
}
