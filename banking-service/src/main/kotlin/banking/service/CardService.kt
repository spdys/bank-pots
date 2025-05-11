package banking.service

import banking.BankingBadRequestException
import banking.BankingNotFoundException
import banking.entity.CardEntity
import banking.entity.CardType
import banking.repository.CardRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@Service
class CardService(
    private val cardRepository: CardRepository,
) {

    fun autoGeneratePhysicalCard(accountId: Long): CardEntity {

        val card = CardEntity(
            accountId = accountId,
            cardNumber = generateCardNumber(), cardType = CardType.PHYSICAL
        )
        return cardRepository.save(card)
    }

    fun getCardById(id: Long): CardEntity {
        val card = cardRepository.findById(id).orElseThrow {
            BankingNotFoundException("Card with ID $id not found")
        }
        return card
    }

    fun createCardManually(card: CardEntity): CardEntity {
        val cardNumber = generateCardNumber()
        val potId = card.potId ?: throw BankingNotFoundException("No pot found!")
        val token = generateToken(potId)
        card.cardNumber = cardNumber
        card.token = token
        return cardRepository.save(card)
    }

    // TODO: associate with a card number for frontend
    fun autoGenerateTokenizedCard(potId: Long): CardEntity {
        val tokenCard = CardEntity(potId = potId, token = generateToken(potId), cardType = CardType.TOKENIZED)
        return cardRepository.save(tokenCard)
    }

    fun deleteCard(id: Long) {
        val existingCard = getCardById(id)
        cardRepository.delete(existingCard)
    }

    private fun generateCardNumber(): String {
        val firstDigit = (1..9).random()
        val remainingDigits = (1..15).map { Random.nextInt(0, 10) }.joinToString("")
        return "$firstDigit$remainingDigits"
    }


    fun generateToken(potId: Long): String {
        val uuidPart = UUID.randomUUID().toString().replace("-", "").take(12)
        return "P${potId}_$uuidPart"
    }

    }

