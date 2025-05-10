package banking.service

import banking.BankingBadRequestException
import banking.BankingForbiddenException
import banking.BankingNotFoundException
import banking.dto.CardPaymentResponse
import banking.entity.AccountEntity
import banking.entity.CardEntity
import banking.entity.CardType
import banking.entity.PotEntity
import banking.entity.TransactionEntity
import banking.repository.AccountRepository
import banking.repository.CardRepository
import banking.repository.PotRepository
import banking.repository.TransactionRepository
import banking.security.UserPrincipal
import banking.dto.DepositSalaryResponse
import banking.dto.PotDepositResponse
import banking.dto.PotWithdrawalResponse
import banking.dto.TransactionHistoryResponse
import banking.dto.toHistoryResponse
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val cardService: CardService,
    private val potRepository: PotRepository,
    private val accountRepository: AccountRepository,
    private val cardRepository: CardRepository,
) {

    private val logger = LoggerFactory.getLogger(TransactionService::class.java)
    // usage of @Transactional here is critical here because if the function fails at some point,
    // all database changes (pot balance, account balance, and transaction record)
    // will be rolled back automatically.

    @Transactional
    fun depositSalaryToAccount(destinationId: Long, amount: BigDecimal): DepositSalaryResponse {

        val destinationAccount = accountRepository.findById(destinationId)
            .orElseThrow { BankingNotFoundException("NO DESTINATION ACCOUNT FOUND") }

        if (destinationAccount.accountType != AccountEntity.AccountType.MAIN){
            throw BankingBadRequestException("NOT MAIN ACCOUNT")
        }
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


        try {
            autoDistributeToPots(destinationAccount)
        } catch (ex: Exception) {
            logger.error("Auto-distribution failed for account ${destinationAccount.id}: ${ex.message}")
        }

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
            // to make sure pots do not exceed main acc balance
            validateTotalPotAllocations(pots, destinationAccount.balance)
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
                    transactionRepository.save(
                        TransactionEntity(
                            sourceId = pot.id,
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

    fun validateTotalPotAllocations(pots: List<PotEntity>, accountBalance: BigDecimal) {
        var total = BigDecimal.ZERO
        for (pot in pots) {
            val allocation = when (pot.allocationType) {
                PotEntity.AllocationType.FIXED -> pot.allocationValue
                PotEntity.AllocationType.PERCENTAGE ->
                    pot.allocationValue.multiply(accountBalance)
            }
            total = total.plus(allocation)
        }

        if (total > accountBalance) {
            throw BankingBadRequestException("Total pot allocations ($total) exceed account balance ($accountBalance).")
        }
    }


    @Transactional
    fun withdrawFromPotToMain(
        sourcePotId: Long,
        amount: BigDecimal,
        principal: UserPrincipal
    ): PotWithdrawalResponse {

        // check existence before retrieving entity > better performance
        if (!potRepository.existsById(sourcePotId)){
            throw BankingNotFoundException("No pot found for sourceId")
        }

        val pot = potRepository.findById(sourcePotId).get()

        val parentAccountId = pot.accountId!!


        if (!accountRepository.existsById(parentAccountId)){
            throw BankingNotFoundException("Destination account not found with id $parentAccountId")
        }

        val potParentAccount = accountRepository.findById(parentAccountId)
            .orElseThrow { BankingNotFoundException("Account not found with id $parentAccountId") }
        val potParentAccountUserId = potParentAccount.userId

        // check authorization claim
        if (potParentAccountUserId != principal.getId()){
            throw BankingForbiddenException("User ID mismatch.")
        }
        // validate amount
        if (amount > pot.balance) {
            throw BankingBadRequestException("Withdrawal amount exceeds pot balance.")
        }
        if (amount <= BigDecimal.ZERO) {
            throw BankingBadRequestException("Withdrawal amount must be non-negative.")
        }
        val potBalanceBefore = pot.balance

        // Update balances
        pot.balance -= amount
        potRepository.save(pot)


        potParentAccount.balance += amount
        accountRepository.save(potParentAccount)
        val newAccountBalance = potParentAccount.balance

        val transaction = TransactionEntity(
            sourceId = pot.id,
            destinationId = potParentAccount.id,
            amount = amount,
            balanceBefore = potBalanceBefore,
            balanceAfter = pot.balance,
            transactionType = TransactionEntity.TransactionType.WITHDRAW,
        )

        transactionRepository.save(transaction)
        return PotWithdrawalResponse(
            newPotBalance = pot.balance,
            newAccountBalance = newAccountBalance
        )
    }

    @Transactional
    fun manualDepositFromMainOrSavingsToPot(
        sourceAccountId: Long,
        destinationPotId: Long,
        amount: BigDecimal,
        principal: UserPrincipal): PotDepositResponse {

        // check existence before retrieving entity > better performance
        if (!accountRepository.existsById(sourceAccountId)) {
            throw BankingNotFoundException("Source account not found with id $sourceAccountId")
        }

        if (!potRepository.existsById(destinationPotId)) {
            throw BankingNotFoundException("Destination pot not found with id $destinationPotId")
        }

        val sourceAccount = accountRepository.findById(sourceAccountId)
            .orElseThrow { BankingNotFoundException("Account not found with id $sourceAccountId") }

        val pot = potRepository.findById(destinationPotId)
            .orElseThrow { BankingNotFoundException("Pot not found with id $destinationPotId") }

        // authorization
        if (sourceAccount.userId != principal.getId()) {
            throw BankingForbiddenException("User ID mismatch.")
        }

        // validate pot belongs to the account
        if (pot.accountId != sourceAccountId) {
            throw BankingBadRequestException("Pot does not belong to the specified account.")
        }

        // validate amount
        if (amount > sourceAccount.balance) {
            throw BankingBadRequestException("Transfer amount exceeds account balance.")
        }
        if (amount <= BigDecimal.ZERO) {
            throw BankingBadRequestException("Transfer amount must be non-negative.")
        }

        val accountBalanceBefore = sourceAccount.balance

        // Update balances
        pot.balance += amount
        potRepository.save(pot)

        sourceAccount.balance -= amount
        accountRepository.save(sourceAccount)

        val transaction = TransactionEntity(
            sourceId = sourceAccount.id,
            destinationId = pot.id,
            amount = amount,
            balanceBefore = accountBalanceBefore,
            balanceAfter = sourceAccount.balance,
            transactionType = TransactionEntity.TransactionType.TRANSFER,
            description = "Manual transfer from account to pot: '${pot.name}'"
        )

        transactionRepository.save(transaction)

        return PotDepositResponse(
            newPotBalance = pot.balance,
            newAccountBalance = sourceAccount.balance
        )

    }

    // Make purchases using physical card or tokenized pot card
    @Transactional
    fun cardPurchase(
        // TODO make description input???
        cardNumberOrToken: String,
        amount: BigDecimal,
        destinationId: Long,
        principal: UserPrincipal
    ): CardPaymentResponse {

        val card = cardRepository.findByCardNumber(cardNumberOrToken)
            ?: cardRepository.findByToken(cardNumberOrToken)
            ?: throw BankingNotFoundException("Card not found.")

        if (!isCardValid(card)) {
            throw BankingBadRequestException("Card is not valid.")
        }

        val isPot = card.accountId == null
        val sourceId: Long
        val sourceBalance: BigDecimal
        val newBalance: BigDecimal

        if (isPot) {
            val pot = potRepository.findById(card.potId!!).orElseThrow {
                BankingNotFoundException("Pot not found for card.")
            }
            val parentAccount = accountRepository.findById(pot.accountId!!).orElseThrow {
                BankingNotFoundException("Account not found for pot.")
            }
            if (parentAccount.userId != principal.getId()) {
                throw BankingForbiddenException("User ID mismatch.")
            }

            sourceBalance = pot.balance

            if (amount > sourceBalance) {
                throw BankingBadRequestException("Purchase amount exceeds pot balance.")
            }

            newBalance = sourceBalance - amount
            pot.balance = newBalance
            potRepository.save(pot)
            sourceId = pot.id
        } else {
            val account = accountRepository.findById(card.accountId!!).orElseThrow {
                BankingNotFoundException("Account not found for card.")
            }
            if (account.userId != principal.getId()) {
                throw BankingForbiddenException("User ID mismatch.")
            }

            sourceBalance = account.balance

            if (amount > sourceBalance) {
                throw BankingBadRequestException("Purchase amount exceeds account balance.")
            }

            newBalance = sourceBalance - amount
            account.balance = newBalance
            accountRepository.save(account)
            sourceId = account.id!!
        }

        val transaction = TransactionEntity(
            sourceId = sourceId,
            cardId = card.id,
            amount = amount,
            description = "POS",
            transactionType = TransactionEntity.TransactionType.PURCHASE,
            balanceBefore = sourceBalance,
            balanceAfter = newBalance,
            destinationId = destinationId,
        )
        transactionRepository.save(transaction)

        return CardPaymentResponse(newBalance = newBalance)
    }

    fun isCardValid(card: CardEntity) : Boolean {
        if (!card.isActive)
            return false
        if (card.expiresAt.isBefore(LocalDateTime.now())) {
            card.isActive = false
            // Safe assertions here
            // as cards creation requires either pot/acc id
            if (card.cardType == CardType.PHYSICAL){
                cardService.autoGeneratePhysicalCard(card.accountId!!)
            }
            if (card.cardType == CardType.TOKENIZED){
                cardService.autoGenerateTokenizedCard(card.potId!!)
            }
            return false
        }
        return true
    }

    // transaction history per account/pot/card
    // transaction history per account/pot/card
    fun transactionHistory(
        accountId: Long? = null,
        potId: Long? = null,
        cardId: Long? = null,
        principal: UserPrincipal
    ): List<TransactionHistoryResponse> {

        // ensure only one non-null is passed
        val nonNullIds = listOf(cardId, potId, accountId).count { it != null }
        if (nonNullIds != 1) {
            throw BankingBadRequestException("You must provide exactly one of: cardId, potId, or accountId.")
        }
        val userId = principal.getId()

        val transactions = when {
            cardId != null -> {
                val card = cardRepository.findById(cardId)
                    .orElseThrow { BankingNotFoundException("Card not found") }

                val claimUserId = if (card.accountId != null) {
                    accountRepository.findById(card.accountId!!)
                        .orElseThrow { BankingNotFoundException("Account not found") }
                        .userId
                } else {
                    val pot = potRepository.findById(card.potId!!)
                        .orElseThrow { BankingNotFoundException("Pot not found") }
                    accountRepository.findById(pot.accountId!!)
                        .orElseThrow { BankingNotFoundException("Account not found for pot") }
                        .userId
                }

                if (claimUserId != userId) {
                    throw BankingForbiddenException("User ID mismatch.")
                }

                transactionRepository.findAllByCardId(cardId)
            }

            potId != null -> {
                val pot = potRepository.findById(potId)
                    .orElseThrow { BankingNotFoundException("Pot not found") }
                val claimUserId = accountRepository.findById(pot.accountId!!)
                    .orElseThrow { BankingNotFoundException("Account not found for pot") }
                    .userId

                if (claimUserId != userId) {
                    throw BankingForbiddenException("User ID mismatch.")
                }

                transactionRepository.findAllBySourceId(potId)
            }

            accountId != null -> {
                val account = accountRepository.findById(accountId)
                    .orElseThrow { BankingNotFoundException("Account not found") }

                if (account.userId != userId) {
                    throw BankingForbiddenException("User ID mismatch.")
                }

                transactionRepository.findAllBySourceId(accountId)
            }

            else -> throw BankingBadRequestException("Must provide cardId, potId, or accountId.")
        }

        return transactions.map { it.toHistoryResponse() }
    }

}