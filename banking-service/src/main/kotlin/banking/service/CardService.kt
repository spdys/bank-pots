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
        checkAndMarkExpired(card)
        return card
    }

    fun createCard(card: CardEntity): CardEntity {
        val cardNumber = generateCardNumber()
        val token = generateToken()
        card.cardNumber = cardNumber
        card.token = token
        return cardRepository.save(card)
    }

    // TODO: maybe need card number?
    fun autoGenerateTokenizedCard(potId: Long): CardEntity {
        val tokenCard = CardEntity(potId = potId, token = generateToken(potId), cardType = CardType.TOKENIZED)
        return cardRepository.save(tokenCard)
    }

    fun deleteCard(id: Long) {
        val existingCard = getCardById(id)
        cardRepository.delete(existingCard)
    }

    private fun checkAndMarkExpired(card: CardEntity) {
        if (card.expiresAt.isBefore(LocalDateTime.now()) && card.isActive) {
            card.isActive = false
            cardRepository.save(card)
        }
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


    private fun generateToken(): String {
        return UUID.randomUUID().toString().replace("-", "").take(16)
    }

    fun checkCardStatus(id: Long, date: LocalDateTime? = null): CardEntity {
        val actualDate = date ?: LocalDateTime.now()
        val card = getCardById(id)

        if (card.expiresAt.isBefore(actualDate)) {
            if (card.isActive) {
                card.isActive = false
                cardRepository.save(card)
            }
            throw BankingBadRequestException("Card expired. Please visit the nearest branch to renew.")
        }

        return card
    }
}
