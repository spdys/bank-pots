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
import banking.repository.PotRepository
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val potRepository: PotRepository
) {
    fun createAccount(request: CreateAccountRequest): AccountResponse {
        // check if user already has a main account, throw exception if so
        val existingMain = accountRepository.findByUserId(request.userId)
            .any { it.accountType == AccountEntity.AccountType.MAIN }

        if (request.accountType == AccountEntity.AccountType.MAIN && existingMain) {
            throw BankingBadRequestException("User already has a main account.")
        }
        // generate account number e.g ACC12345678
        val randomDigits = (10000000..99999999).random()
        val accountNumber = "ACC$randomDigits"

        val account = AccountEntity(
            userId = request.userId,
            accountNumber = accountNumber,
            accountType = request.accountType
        )

        val savedAccount = accountRepository.save(account)

        return AccountResponse(
            savedAccount.id,
            savedAccount.accountNumber,
            savedAccount.accountType,
            savedAccount.balance,
            savedAccount.createdAt
        )
    }

    fun closeAccount(accountId: Long): CloseAccountResponse {
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

    fun getAccountSummary(accountId: Long): AccountSummaryDto {
        val account = accountRepository.findById(accountId)
            .orElseThrow { BankingNotFoundException("Account not found with id $accountId.") }

        // if account is "MAIN"
        return if (account.accountType == AccountEntity.AccountType.MAIN) {
            val pots = potRepository.findByAccountId(account.id)
                .map { pot ->
                    PotSummaryDto(
                        pot.id,
                        pot.name,
                        pot.balance,
                        pot.allocationType,
                        pot.allocationValue
                    )
                }

            AccountSummaryDto(
                account.id,
                account.accountNumber,
                account.accountType,
                account.balance,
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
                account.currency,
                account.isActive,
            )
        }
    }
}
