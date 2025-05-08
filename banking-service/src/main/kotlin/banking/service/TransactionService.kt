package banking.service

import banking.BankingBadRequestException
import banking.BankingNotFoundException
import banking.entity.AccountEntity
import banking.entity.TransactionEntity
import banking.repository.AccountRepository
import banking.repository.PotRepository
import banking.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val cardService: CardService,
    private val potRepository: PotRepository,
    private val accountRepository: AccountRepository
) {

    private fun calculateBalance(currentBalance: Double, amount: Double, isDeposit: Boolean): Double {
        return if (isDeposit) currentBalance + amount else currentBalance - amount
    }

    fun deposit(accountId: Long, amount: Double, isSalary: Boolean = false, description: String? = null): TransactionEntity {
        val now = LocalDateTime.now()

        //  if the account exists, retrieve its type
        val account = accountRepository.findById(accountId).orElseThrow {
            BankingNotFoundException("Account with ID $accountId not found")
        }

        //  if it is a salary deposit enforce deposit to  main
        if (isSalary && account.accountType != AccountEntity.AccountType.MAIN) {
            throw BankingBadRequestException("Salary can only be deposited into the main account")
        }

        val currentBalance = 1000.0  // to be replaced
        val updatedBalance = calculateBalance(currentBalance, amount, true)

        val transaction = TransactionEntity(
            cardId = accountId,
            sourceId = null,
            destinationId = accountId,
            amount = amount,
            transactionType = if (isSalary) "SALARY_DEPOSIT" else "DEPOSIT",
            description = description ?: "No description",
            balance = updatedBalance,
            createdAt = now
        )
        return transactionRepository.save(transaction)
    }

    fun transfer(cardNumber: Long, amount: Double, destinationId: Long, description: String? = null, date: LocalDateTime? = null): TransactionEntity {
        val now = date ?: LocalDateTime.now()

        val card = cardService.getCardById(cardNumber) // get card info

        //  card is associated with an account or a pot?
        val isAccount = card.cardType == "PHYSICAL"
        val isPot = card.cardType == "VIRTUAL"

        if (isAccount) {
            if (card.cardNumber == null) {
                throw BankingBadRequestException("Invalid transfer: Expected account card number")
            }
        } else if (isPot) {
            if (card.token == null) {
                throw BankingBadRequestException("Invalid transfer: Expected pot token")
            }
        } else {
            throw BankingBadRequestException("Unknown card type: ${card.cardType}")
        }

        val currentBalance = 1000.0 // to be replace
        val updatedBalance = calculateBalance(currentBalance, amount, false)

        if (updatedBalance < 0) throw BankingBadRequestException("Insufficient funds")

        val transaction = TransactionEntity(
            cardId = cardNumber,
            sourceId = cardNumber,
            destinationId = destinationId,
            amount = amount,
            transactionType = "TRANSFER",
            description = description ?: "No description",
            balance = updatedBalance,
            createdAt = now
        )
        return transactionRepository.save(transaction)
    }

    fun withdraw(cardId: Long, amount: Double, description: String? = null, date: LocalDateTime? = null): TransactionEntity {
        val now = date ?: LocalDateTime.now()
        val card = cardService.getCardById(cardId) // get card info

        val currentBalance = 1000.0 //to be replaced
        val updatedBalance = calculateBalance(currentBalance, amount, false)

        if (updatedBalance < 0) {
            throw BankingBadRequestException("Insufficient funds for withdrawal")
        }

        val transaction = TransactionEntity(
            cardId = cardId,
            sourceId = cardId,
            destinationId = null, // withdrawals have no destination
            amount = amount,
            transactionType = "WITHDRAWAL",
            description = description ?: "No description",
            balance = updatedBalance,
            createdAt = now
        )
        return transactionRepository.save(transaction)
    }
    fun getTransactions(accountId: Long, limit: Int): List<TransactionEntity> {
        return transactionRepository.findTop10ByDestinationIdOrderByCreatedAtDesc(accountId)
    }
}