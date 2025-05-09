package banking.service

import banking.BankingNotFoundException
import banking.entity.AccountEntity
import banking.entity.PotEntity
import banking.entity.TransactionEntity
import banking.repository.AccountRepository
import banking.repository.PotRepository
import banking.repository.TransactionRepository
import com.banking.bankingservice.dto.DepositSalaryResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val cardService: CardService,
    private val potRepository: PotRepository,
    private val accountRepository: AccountRepository
) {
    /*
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    PURCHASE,
    */
    @Transactional
    fun depositSalaryToAccount(destinationId: Long, amount: BigDecimal): DepositSalaryResponse {

        val destinationAccount = accountRepository.findById(destinationId)
            .orElseThrow { BankingNotFoundException("NO DESTINATION ACCOUNT FOUND") }

        val balanceBefore = destinationAccount.balance
        val balanceAfter = balanceBefore.plus(amount)
        val description = "SALARY"

        val transaction = TransactionEntity(
            destinationId = destinationId,
            amount = amount,
            description = description,
            transactionType = TransactionEntity.TransactionType.DEPOSIT,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
        )
        transactionRepository.save(transaction)

        destinationAccount.balance = balanceAfter
        accountRepository.save(destinationAccount)


        autoDistributeToPots(destinationAccount)

        // Edit This TODO?
        return DepositSalaryResponse(
            destinationId = destinationId,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter
        )


    }

    fun autoDistributeToPots(destinationAccount: AccountEntity) {
        // check if main
        val destinationOriginalBalance = destinationAccount.balance
        if (destinationAccount.accountType == AccountEntity.AccountType.MAIN) {
            // fetch all pots
            val pots = potRepository.findAllByAccountId(destinationAccount.id)
            var potsTotalAllocationAmount = BigDecimal.ZERO
            // if no pots exist, it will skip for loop
            // loop through pots to distribute based on fixed/percentage
            for (pot in pots) {
                val allocationPerPot = when (pot.allocationType) {
                    PotEntity.AllocationType.FIXED -> pot.allocationValue
                    PotEntity.AllocationType.PERCENTAGE -> pot.allocationValue
                        .multiply(destinationAccount.balance)
                }
                if (allocationPerPot > BigDecimal.ZERO) {
                    val potBalanceBefore = pot.balance
                    val potBalanceAfter = potBalanceBefore.plus(allocationPerPot)
                    pot.balance = potBalanceAfter
                    potRepository.save(pot)
                    transactionRepository.save(TransactionEntity(
                        destinationId = destinationAccount.id,
                        amount = allocationPerPot,
                        description = "Auto transfer from SALARY to ${pot.name}",
                        transactionType = TransactionEntity.TransactionType.TRANSFER,
                        balanceBefore = potBalanceBefore,
                        balanceAfter = pot.balance
                    )

                    )
                    potsTotalAllocationAmount = potsTotalAllocationAmount.plus(allocationPerPot)
                }
            }
            // Main account updated balance
            destinationAccount.balance = destinationAccount.balance.minus(potsTotalAllocationAmount)
            accountRepository.save(destinationAccount)
        }

    }


//
//    private fun calculateBalance(currentBalance: Double, amount: Double, isDeposit: Boolean): Double {
//        return if (isDeposit) currentBalance + amount else currentBalance - amount
//    }
//
//    fun deposit(accountId: Long, amount: Double, isSalary: Boolean = false, description: String? = null): TransactionEntity {
//        val now = LocalDateTime.now()
//
//        //  if the account exists, retrieve its type
//        val account = accountRepository.findById(accountId).orElseThrow {
//            BankingNotFoundException("Account with ID $accountId not found")
//        }
//
//        //  if it is a salary deposit enforce deposit to  main
//        if (isSalary && account.accountType != AccountEntity.AccountType.MAIN) {
//            throw BankingBadRequestException("Salary can only be deposited into the main account")
//        }
//
//        val currentBalance = 1000.0  // to be replaced
//        val updatedBalance = calculateBalance(currentBalance, amount, true)
//
//        val transaction = TransactionEntity(
//            cardId = accountId,
//            sourceId = null,
//            destinationId = accountId,
//            amount = amount,
//            transactionType = if (isSalary) "SALARY_DEPOSIT" else "DEPOSIT",
//            description = description ?: "No description",
//            balance = updatedBalance,
//            createdAt = now
//        )
//        return transactionRepository.save(transaction)
//    }
//
//    fun transfer(cardNumber: Long, amount: Double, destinationId: Long, description: String? = null, date: LocalDateTime? = null): TransactionEntity {
//        val now = date ?: LocalDateTime.now()
//
//        val card = cardService.getCardById(cardNumber) // get card info
//
//        //  card is associated with an account or a pot?
//        val isAccount = card.cardType == "PHYSICAL"
//        val isPot = card.cardType == "VIRTUAL"
//
//        if (isAccount) {
//            if (card.cardNumber == null) {
//                throw BankingBadRequestException("Invalid transfer: Expected account card number")
//            }
//        } else if (isPot) {
//            if (card.token == null) {
//                throw BankingBadRequestException("Invalid transfer: Expected pot token")
//            }
//        } else {
//            throw BankingBadRequestException("Unknown card type: ${card.cardType}")
//        }
//
//        val currentBalance = 1000.0 // to be replace
//        val updatedBalance = calculateBalance(currentBalance, amount, false)
//
//        if (updatedBalance < 0) throw BankingBadRequestException("Insufficient funds")
//
//        val transaction = TransactionEntity(
//            cardId = cardNumber,
//            sourceId = cardNumber,
//            destinationId = destinationId,
//            amount = amount,
//            transactionType = "TRANSFER",
//            description = description ?: "No description",
//            balance = updatedBalance,
//            createdAt = now
//        )
//        return transactionRepository.save(transaction)
//    }
//
//    fun withdraw(cardId: Long, amount: Double, description: String? = null, date: LocalDateTime? = null): TransactionEntity {
//        val now = date ?: LocalDateTime.now()
//        val card = cardService.getCardById(cardId) // get card info
//
//        val currentBalance = 1000.0 //to be replaced
//        val updatedBalance = calculateBalance(currentBalance, amount, false)
//
//        if (updatedBalance < 0) {
//            throw BankingBadRequestException("Insufficient funds for withdrawal")
//        }
//
//        val transaction = TransactionEntity(
//            cardId = cardId,
//            sourceId = cardId,
//            destinationId = null, // withdrawals have no destination
//            amount = amount,
//            transactionType = "WITHDRAWAL",
//            description = description ?: "No description",
//            balance = updatedBalance,
//            createdAt = now
//        )
//        return transactionRepository.save(transaction)
//    }
//    fun getTransactions(accountId: Long, limit: Int): List<TransactionEntity> {
//        return transactionRepository.findTop10ByDestinationIdOrderByCreatedAtDesc(accountId)
//    }
}