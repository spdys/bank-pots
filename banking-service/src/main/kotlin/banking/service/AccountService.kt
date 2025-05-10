package banking.service

import banking.BankingBadRequestException
import banking.BankingNotFoundException
import banking.dto.CreateAccountRequest
import banking.dto.AccountResponse
import banking.dto.AccountSummaryDto
import banking.dto.CloseAccountResponse
import banking.dto.PotSummaryDto
import banking.entity.AccountEntity
import banking.repository.AccountRepository
import banking.repository.CardRepository
import banking.repository.PotRepository
import banking.security.UserPrincipal
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val potRepository: PotRepository,
    private val cardService: CardService,
    private val cardRepository: CardRepository

) {
    fun createAccount(request: CreateAccountRequest, principal: UserPrincipal): AccountResponse {
        // check if user already has a main account, throw exception if so
        val existingMain = accountRepository.findAllByUserId(principal.getId() ?: 0)
            .any { it.accountType == AccountEntity.AccountType.MAIN }

        if (request.accountType == AccountEntity.AccountType.MAIN && existingMain) {
            throw BankingBadRequestException("User already has a main account.")
        }
        // generate account number e.g ACC12345678
        val randomDigits = (10000000..99999999).random()
        val accountNumber = "ACC$randomDigits"

        val account = AccountEntity(
            userId = principal.getId() ?: 0,
            accountNumber = accountNumber,
            accountType = request.accountType
        )

        val savedAccount = accountRepository.save(account)

        // need to autogenerate physical card after account creation
        cardService.autoGeneratePhysicalCard(savedAccount.id)

        return AccountResponse(
            savedAccount.id,
            savedAccount.accountNumber,
            savedAccount.accountType,
            savedAccount.balance,
            savedAccount.createdAt
        )
    }

    fun getAccountSummary(accountId: Long, principal: UserPrincipal): AccountSummaryDto {
        // checking if account exists
        val account = accountRepository.findById(accountId)
            .orElseThrow { BankingNotFoundException("Account not found with id $accountId.") }

        // check if accountId is associated with principal's ID
        if(account.userId != principal.getId()) {
            throw BankingNotFoundException("User ID mismatch.")
        }
        val accountCard = cardRepository.findByAccountId(account.id)

        // if account is "MAIN"
        return if (account.accountType == AccountEntity.AccountType.MAIN) {
            val pots = potRepository.findByAccountId(account.id)
                .map { pot ->
                    PotSummaryDto(
                        pot.id,
                        pot.name,
                        pot.balance,
                        cardRepository.findByPotId(pot.id).token!!,
                        pot.allocationType,
                        pot.allocationValue
                    )
                }

            AccountSummaryDto(
                account.id,
                account.accountNumber,
                account.accountType,
                account.balance,
                accountCard.cardNumber!!,
                account.currency,
                account.isActive,
                pots,
            )
        } else { // if account is "SAVINGS"
            AccountSummaryDto(
                account.id,
                account.accountNumber,
                account.accountType,
                account.balance,
                accountCard.cardNumber!!,
                account.currency,
                account.isActive,
            )
        }
    }
    fun closeAccount(accountId: Long): CloseAccountResponse {
        // checking if account exists
        val account = accountRepository.findById(accountId)
            .orElseThrow { BankingNotFoundException("Account not found.") }

        val updatedAccount = account.copy(isActive = false)
        accountRepository.save(updatedAccount)

        return CloseAccountResponse(
            updatedAccount.id,
            updatedAccount.accountNumber,
            updatedAccount.accountType,
            updatedAccount.isActive
        )
    }
}
